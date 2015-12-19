package pl.kask.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import pl.kask.model.Account;
import pl.kask.model.AccountDao;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

public class VerificationService {

    private final GoogleIdTokenVerifier verifier;
    JsonFactory jsonFactory;

    private final AccountDao accountDao;

    public VerificationService(String clientId, AccountDao accountDao) {
        this.accountDao = accountDao;
        NetHttpTransport transport = new NetHttpTransport();

        jsonFactory = new GsonFactory();
        verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Arrays.asList(clientId))
                .setIssuer("https://accounts.google.com")
                .build();
    }

    private GoogleIdToken.Payload verify(String idTokenString) throws GeneralSecurityException, IOException {
        if (idTokenString == null) {
            System.out.println("Token string is null");
            return null;
        }
        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();
            System.out.println(payload);
            return payload;
        } else {
            System.out.println("Google Token is null");
            return null;
        }
    }

    public boolean isVerified(String idToken, String name) {
        try {
            GoogleIdToken.Payload payload = verify(idToken);
            if (payload != null) {
                String subject = payload.getSubject();
                Account account = accountDao.findByGoogleId(subject);
                if (account == null) {
                    account = new Account(subject, payload.getEmail());
                    accountDao.persist(account);
                }
                System.out.println(subject);
                if (subject.equals(name)) {
                    return true;
                }
            } else {
                System.out.println("Payload is null");
            }
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
