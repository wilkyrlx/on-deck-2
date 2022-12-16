package edu.brown.cs.student.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.server.handlers.DefaultHandler;
import edu.brown.cs.student.server.handlers.ImportantGamesHandler;
import edu.brown.cs.student.server.handlers.SportsHandler;
import edu.brown.cs.student.util.Scorer;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

public class APITest {
  private static final Moshi moshi = new Moshi.Builder().build();
  private static final Scorer s = new Scorer(moshi);

  @BeforeAll
  public static void setup_before_everything() {
    // Setting port 0 will cause Spark to use an arbitrary available port.
    Spark.port(0);
    Logger.getLogger("").setLevel(Level.WARNING); // empty name = root logger
  }

  /**
   * Shared state for all tests. We clear this state out after every test runs.
   */
  @BeforeEach
  public void setup() {
    Spark.get("sports", new SportsHandler(moshi, s));
    Spark.get("important", new ImportantGamesHandler(moshi, s));
    Spark.get("*", new DefaultHandler());
    Spark.init();
    Spark.awaitInitialization(); // don't continue until the server is listening
  }

  @AfterEach
  public void teardown() {
    // Gracefully stop Spark listening on all endpoints
    Spark.unmap("/sports");

    Spark.awaitStop(); // don't proceed until the server is stopped
  }

  /**
   * Helper to start a connection to a specific API endpoint/params
   * @param apiCall the call string, including endpoint
   *                (NOTE: this would be better if it had more structure!)
   * @return the connection for the given URL, just after connecting
   * @throws IOException if the connection fails for some reason
   */
  static private HttpURLConnection tryRequest(String apiCall) throws IOException {
    // Configure the connection (but don't actually send the request yet)
    URL requestURL = new URL("http://localhost:"+Spark.port()+"/"+apiCall);
    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();

    // The default method is "GET", which is what we're using here.
    // If we were using "POST", we'd need to say so.
    //clientConnection.setRequestMethod("GET");

    clientConnection.connect();
    return clientConnection;
  }

  /**
   * Helper function to read JSON from moshi. Used for testing
   *
   * @param clientConnection - the HTTP URL connection resulting from sending an API query
   * @return a map of String, Object pairs generated by moshi
   */
  static private Map<String, Object> getResponse(HttpURLConnection clientConnection) throws IOException {
    StringBuilder sb = new StringBuilder();
    // https://stackoverflow.com/questions/309424/how-do-i-read-convert-an-inputstream-into-a-string-in-java
    for (int ch; (ch = clientConnection.getInputStream().read()) != -1; ) {
      sb.append((char) ch);
    }
    String jsonResp = sb.toString();
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(Types.newParameterizedType(Map.class,
        String.class, Object.class));
    return adapter.fromJson(jsonResp);
  }

  /** Tests the general call for the Boston Celtics (in the basketball league) */
  @Test
  public void testBasicCallCeltics() throws IOException {
    String exCall = "sports?sport=basketball&league=nba&team=boston-celtics";
    HttpURLConnection clientConnection = tryRequest(exCall);
    Map<String, Object> response = getResponse(clientConnection);

    Assertions.assertNotNull(response);
    assertTrue(response.get("eventList") instanceof List);
    List<Map<String, String>> eventList = (List<Map<String, String>>)response.get("eventList");
    
    assertEquals(response.get("result"), "success");
    assertEquals(response.get("displayName"),"Boston Celtics");
    assertEquals(eventList.get(0).get("name"),
        "Philadelphia 76ers at Boston Celtics");
    assertEquals(eventList.get(1).get("homeTeamName"),
        "Miami Heat");
    clientConnection.disconnect();
  }

  /**
   * Tests the general call for a team in the american football league
   */
  @Test
  public void testBasicCallNfl() throws IOException {
    String exCall = "sports?sport=football&league=nfl&team=pittsburgh-steelers";
    HttpURLConnection clientConnection = tryRequest(exCall);
    Map<String, Object> response = getResponse(clientConnection);

    Assertions.assertNotNull(response);
    assertEquals(response.get("displayName"),"Pittsburgh Steelers");
    assertEquals(response.get("logo"),"https://a.espncdn.com/i/teamlogos/nfl/500/pit.png");
    assertEquals(response.get("color"),"000000");
    assertEquals(((Map<String, String>)response.get("game5")).get("gameName"), "Tampa Bay Buccaneers at Pittsburgh Steelers");
    clientConnection.disconnect();
  }

  /**
   * Tests the general call for a team in the american baseball league
   */
  @Test
  public void testBasicCallMlb() throws IOException {
    String exCall = "sports?sport=baseball&league=mlb&team=boston-red-sox";
    HttpURLConnection clientConnection = tryRequest(exCall);
    Map<String, Object> response = getResponse(clientConnection);

    Assertions.assertNotNull(response);
    assertEquals(response.get("displayName"),"Boston Red Sox");
    assertEquals(response.get("logo"),"https://a.espncdn.com/i/teamlogos/mlb/500/bos.png");
    assertEquals(response.get("color"),"00224b");
    assertEquals(((Map<String, String>)response.get("game5")).get("gameName"), "Pittsburgh Pirates at Boston Red Sox");
    clientConnection.disconnect();
  }

  /**
   * Tests the general call for a team in the american hockey league
   */
  @Test
  public void testBasicCallNhl() throws IOException {
    String exCall = "sports?sport=hockey&league=nhl&team=boston-bruins";
    HttpURLConnection clientConnection = tryRequest(exCall);
    Map<String, Object> response = getResponse(clientConnection);

    Assertions.assertNotNull(response);
    assertEquals(response.get("displayName"),"Boston Bruins");
    assertEquals(response.get("logo"),"https://a.espncdn.com/i/teamlogos/nhl/500/bos.png");
    assertEquals(response.get("color"),"231f20");
    assertTrue(response.get("game5") instanceof Map);
    assertEquals(((Map<String, String>)response.get("game5")).get("gameName"), "Minnesota Wild at Boston Bruins");
    clientConnection.disconnect();
  }

  /**
   * Tests the failure shown when a team does not exist
   */
  @Test
  public void testTeamDoesntExist() throws IOException {
    String exCall = "sports?sport=hockey&league=nhl&team=boston-hockey-players";
    HttpURLConnection clientConnection = tryRequest(exCall);
    Map<String, Object> response = getResponse(clientConnection);

    Assertions.assertNotNull(response);
    assertEquals(response.get("result"), "error_bad_request");
    clientConnection.disconnect();
  }


  /**
   * Tests the failure shown when a sport does not exist
   */
  @Test
  public void testSportDoesntExist() throws IOException {
    String exCall = "sports?sport=quidditch&league=nhl&team=boston-hockey-players";
    HttpURLConnection clientConnection = tryRequest(exCall);
    Map<String, Object> response = getResponse(clientConnection);

    Assertions.assertNotNull(response);
    assertEquals(response.get("result"), "error_bad_request");
    clientConnection.disconnect();
  }

  /**
   * Tests the failure shown when a league does not exist
   */
  @Test
  public void testLeagueDoesntExist() throws IOException {
    String exCall = "sports?sport=hockey&league=canadianhockey&team=boston-hockey-players";
    HttpURLConnection clientConnection = tryRequest(exCall);
    Map<String, Object> response = getResponse(clientConnection);

    Assertions.assertNotNull(response);
    assertEquals(response.get("result"), "error_bad_request");
    clientConnection.disconnect();
  }
}
