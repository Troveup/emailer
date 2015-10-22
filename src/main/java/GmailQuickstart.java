/**
 * Created by tim on 10/21/15.
 */
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListLabelsResponse;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GmailQuickstart {
    /** Application name. */
    private static final String APPLICATION_NAME =
            "Gmail API Java Quickstart";

    /** Directory to store user credentials for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
            System.getProperty("user.home"), ".credentials/gmail-java-quickstart");

    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the scopes required by this quickstart. */
    private static final List<String> SCOPES = new ArrayList<String>();

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =
                GmailQuickstart.class.getResourceAsStream("/client_secret_408490279877-ikbt00vg6n9i2r08b1bip2gftoh6bvqq.apps.googleusercontent.com.json");
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        SCOPES.addAll(GmailScopes.all());

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(DATA_STORE_FACTORY)
                        .setAccessType("offline")
                        .build();
        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver()).authorize("user");
        System.out.println(
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized Gmail client service.
     * @return an authorized Gmail client service
     * @throws IOException
     */
    public static Gmail getGmailService() throws IOException {
        Credential credential = authorize();
        return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static void main(String[] args) throws IOException {
        // Build a new authorized API client service.
        Gmail service = getGmailService();

        // Print the labels in the user's account.
        /*String user = "me";
        ListLabelsResponse listResponse =
                service.users().labels().list(user).execute();
        List<Label> labels = listResponse.getLabels();
        if (labels.size() == 0) {
            System.out.println("No labels found.");
        } else {
            System.out.println("Labels:");
            for (Label label : labels) {
                System.out.printf("- %s\n", label.getName());
            }
        }*/

        try {

            byte[] rawCsvFile = Files.readAllBytes(Paths.get("/Users/tim/Workspace/Java/Trove/emailer/src/main/resources/users.csv"));

            String csvAsString = new String(rawCsvFile, Charset.forName("UTF-8"));

            String[] splitCsv = csvAsString.split("\\n");

            byte[] rawEmailTextFile = Files.readAllBytes(Paths.get("/Users/tim/Workspace/Java/Trove/emailer/src/main/resources/EmailText.txt"));

            for (int i = 0; i < splitCsv.length; ++i)
            {
                String[] rsplit = splitCsv[i].split("\\r");
                splitCsv[i] = rsplit[0];
            }

            //for (int i = 1; i < 2; ++i)
            for (int i = 1; i < splitCsv.length; ++i)
            {
                String[] localSplit = splitCsv[i].split(",");

                //Assumes the first column of any row is a name, then the second column of any row is an e-mail
                String rawEmailTextString = new String(rawEmailTextFile, Charset.forName("UTF-8"));

                rawEmailTextString = rawEmailTextString.replace("{NAME}", localSplit[0]);

                System.out.println("Output Email: " + rawEmailTextString);

                String name = localSplit[0];
                if (localSplit[0].equals("there"))
                    name = localSplit[1];


                MimeMessage message = GmailMessage.createEmail(localSplit[1], name, "kristin@troveup.com", "Kristin", "quick question", rawEmailTextString);
                GmailMessage.sendMessage(service, "kristin@troveup.com", message);

                System.out.println("Message to " + localSplit[1] + " sent!");

                Thread.sleep(3000);
            }

        }   catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String converStreamToString(InputStream is) throws IOException
    {
        String rval = "";

        InputStreamReader reader = new InputStreamReader(is);
        BufferedReader bufferedInputStreamReader = new BufferedReader(reader);
        String readline = bufferedInputStreamReader.readLine();

        while(readline != null)
        {
            rval += readline;
            readline = bufferedInputStreamReader.readLine();
        }

        return rval;
    }

}
