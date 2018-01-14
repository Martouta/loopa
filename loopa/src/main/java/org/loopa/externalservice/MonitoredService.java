package org.loopa.externalservice;

import org.loopa.comm.message.IMessage;
import org.loopa.comm.message.Message;

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import org.json.JSONObject;

public class MonitoredService extends ExternalService {
  private int id;
  private int idConf;
  private String toolName;
  private int timeSlot;
  private String kafkaUrl;
  private String kafkaPort;
  private String kafkaTopic;
  private String keywordExpression;

  public MonitoredService(String id, int idConf, String toolName, int timeSlot, String kafkaUrl, String kafkaTopic, String keywordExpression) {
    super(id);
    this.idConf = idConf;
    this.toolName = toolName;
		this.timeSlot = timeSlot;
		this.kafkaUrl = kafkaUrl;
    this.kafkaPort = "9092";
		this.kafkaTopic = kafkaTopic;
		this.keywordExpression = keywordExpression;
	}

  public String getKafkaEndpoint() {
		return kafkaUrl + ":" + kafkaPort;
	}

  public String getKafkaTopic() {
		return kafkaTopic;
	}

  public void processRequest(IMessage message) {
      String type = message.getMessageType();
      switch (type) {
      case "response":
        int newTimeSlot = Integer.parseInt( message.getMessageBody().get("timeSlot") );
        if (newTimeSlot != -1) { this.timeSlot = newTimeSlot; }
        reconfigureMonitor();
        break;
      default:
        System.err.println("Invalid type code in processRequest");
      }
  }

  private void reconfigureMonitor() {
    delRequestMonitor();
    this.idConf = postRequestMonitor();
    System.out.println("new Monitor idConf: " + idConf);
  }

  private boolean delRequestMonitor() {
    HttpURLConnection httpURLConnection = null;
    boolean worked = false;
    try {
      URL url = new URL("http://supersede.es.atos.net:8081/twitterAPI/configuration/" + this.idConf);
      httpURLConnection = (HttpURLConnection) url.openConnection();
      httpURLConnection.setRequestProperty("Content-Type", "text/plain");
      httpURLConnection.setRequestMethod("DELETE");
      worked = (httpURLConnection.getResponseCode() == 200);
    } catch (MalformedURLException e) {
      e.printStackTrace();
      System.exit(0);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(0);
    }  finally {
      if (httpURLConnection != null) { httpURLConnection.disconnect(); }
    }
    return worked;
  }

  private int postRequestMonitor() {
    HttpURLConnection httpURLConnection = null;
    OutputStreamWriter outputStreamWriter = null;
    int newIdConf = -1;
    try {
      URL url = new URL("http://supersede.es.atos.net:8081/twitterAPI/configuration/");
      httpURLConnection = (HttpURLConnection) url.openConnection();
      httpURLConnection.setRequestProperty("Content-Type", "text/plain");
      httpURLConnection.setRequestMethod("POST");
      httpURLConnection.setDoInput(true);
      httpURLConnection.setDoOutput(true);
      outputStreamWriter = new OutputStreamWriter(httpURLConnection.getOutputStream(), "UTF-8");
      outputStreamWriter.write( this.getReconfigurationParams() );
      outputStreamWriter.flush();
      String returnedDataStr = getText(httpURLConnection);
      JSONObject returnedDataJson = new JSONObject(returnedDataStr).getJSONObject("SocialNetworksMonitoringConfProfResult");
      if (returnedDataJson.getString("status").equals("success")) { newIdConf = returnedDataJson.getInt("idConf"); }
      else { System.err.println("POST error: " + returnedDataStr); }
    } catch (MalformedURLException e) {
      e.printStackTrace();
      System.exit(0);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(0);
    }  finally {
      if (outputStreamWriter != null) {
        try {
          outputStreamWriter.close();
        } catch (IOException e) {
          e.printStackTrace();
          System.exit(0);
        }
      }
      if (httpURLConnection != null) { httpURLConnection.disconnect(); }
    }
    return newIdConf;
  }

  String getText(HttpURLConnection connection) throws IOException {
    // handle error response code it occurs
    int responseCode = connection.getResponseCode();
    InputStream inputStream;
    if (200 <= responseCode && responseCode <= 299) {
      inputStream = connection.getInputStream();
    } else {
      inputStream = connection.getErrorStream();
    }

    BufferedReader in = new BufferedReader( new InputStreamReader( inputStream));

    StringBuilder response = new StringBuilder();
    String currentLine;

    while ((currentLine = in.readLine()) != null) { response.append(currentLine); }

    in.close();

    return response.toString();
  }

  private String getReconfigurationParams(){
    return "{\n"
      + "\t\"SocialNetworksMonitoringConfProf\": {\n"
      + "\t\t\"toolName\": \""+toolName+"\", \n"
      + "\t\t\"timeSlot\": \""+timeSlot+"\", \n"
      + "\t\t\"kafkaEndpoint\": \""+getKafkaEndpoint()+"\", \n"
      + "\t\t\"kafkaTopic\": \""+kafkaTopic+"\", \n"
      + "\t\t\"keywordExpression\": \""+keywordExpression+"\"\n"
      + "\t}\n"
    + "}";
  }

}
