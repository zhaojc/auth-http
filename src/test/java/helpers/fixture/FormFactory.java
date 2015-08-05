package helpers.fixture;

import com.ning.http.client.Param;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tommackenzie on 6/5/15.
 */
public class FormFactory {

    public static List<Param> makeLoginForm(String email, String csrfToken) {
        Param userName = new Param("email", email);
        Param password = new Param("password", "password");
        Param csrfParam = new Param("csrfToken", csrfToken);
        List<Param> postData = new ArrayList<>();
        postData.add(userName);
        postData.add(password);
        postData.add(csrfParam);

        return postData;
    }
}
