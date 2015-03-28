package cz.pitel.pdmuzei;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class PixelDailiesArtSource extends RemoteMuzeiArtSource {
    public PixelDailiesArtSource() {
        super("PDmuzei"); //getString(R.string.app_name)
    }

    @Override
    protected void onTryUpdate(final int reason) throws RetryException {
        if (BuildConfig.DEBUG) Log.d("PDmuzei", "onTryUpdate " + reason);
        HttpsURLConnection PDConnection = null;
        try {
            final String token = getString(R.string.access_token);
            final URL PDURL = new URL("https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name=Pixel_Dailies");
            PDConnection = (HttpsURLConnection) PDURL.openConnection();
            PDConnection.setRequestProperty("Authorization", "Bearer " + token);
            final JSONArray PDJSON = new JSONArray(IOUtils.toString(PDConnection.getInputStream()));
            if (BuildConfig.DEBUG) Log.v("Twitter", PDJSON.toString());
            tweetsloop:
            for (int t = 0; t < PDJSON.length(); t++) {
                final JSONObject tweet = PDJSON.getJSONObject(t).optJSONObject("retweeted_status");
                if (tweet != null) {
                    Log.d("Twitter", tweet.toString());
                    final String text = tweet.getString("text");
                    final JSONObject user = tweet.getJSONObject("user");
                    final String screen_name = user.getString("screen_name");
                    if (BuildConfig.DEBUG) Log.i(screen_name, text);
                    final JSONArray media = tweet.getJSONObject("entities").getJSONArray("media");
                    for (int m = 0; m < media.length(); m++) {
                        final JSONObject photo = media.getJSONObject(m);
                        if (photo.getString("type").equals("photo")) {
                            final String media_url;
                            if (getSharedPreferences().getBoolean("https", false)) {
                                media_url = photo.getString("media_url_https");
                            } else {
                                media_url = photo.getString("media_url");
                            }
                            if (BuildConfig.DEBUG) Log.i(screen_name, media_url);
                            publishArtwork(new Artwork.Builder()
                                    .byline(text)
                                    .title("@" + screen_name)
                                    .imageUri(Uri.parse(media_url))
                                    .viewIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(photo.getString("expanded_url"))))
                                    .build());
                            break tweetsloop;
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RetryException(e);
        } finally {
            if (PDConnection != null) {
                PDConnection.disconnect();
            }
        }
        short minutes;
        try {
            minutes = Short.parseShort(getSharedPreferences().getString("refresh", "60"));
        } catch (final NumberFormatException nfe) {
            minutes = 60;
        }
        if (minutes < 1) {
            minutes = 1;
        }
        scheduleUpdate(System.currentTimeMillis() + minutes * 60 * 1000);
    }
}
