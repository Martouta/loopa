import java.util.*;
import java.sql.Timestamp;
import java.security.SecureRandom;

public class TweetsGenerator {
    public static final String getRandomTweets(){
        String authorName = randomString(10);
        String tweetID = randomInt(17);
        String timeStamp = new Timestamp((new Date()).getTime()).toString();
        return "{"
          +"\"SocialNetworksMonitoredData\": {"
          +"  \"confId\": 1,"
          +"  \"numDataItems\": 1,"
          +"  \"DataItems\": ["
          +"    {"
          +"      \"timeStamp\": \"" + timeStamp + "\","
          +"      \"author\": \"@" + authorName + "\","
          +"      \"link\": \"https://twitter.com/" + authorName + "/status/" + tweetID + "\","
          +"      \"id\": \"" + tweetID + "\","
          +"      \"message\": \"" + randomString(130) + "\""
          +"    }"
          +"  ],"
          +"  \"idOutput\": 46,"
          +"  \"searchTimeStamp\": \"" + timeStamp + "\""
          +"}"
        +"}";
    }

    private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static SecureRandom rnd = new SecureRandom();

    private static final String randomString(int len){
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ ) sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }

    private static final String randomInt(int len) {
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ ) sb.append( AB.charAt( rnd.nextInt(10) ) );
        return sb.toString();
    }
}
