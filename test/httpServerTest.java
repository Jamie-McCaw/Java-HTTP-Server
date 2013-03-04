import org.junit.Test;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.StringReader;
import java.io.StringWriter;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: Psiral
 * Date: 2/20/13
 * Time: 2:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class httpServerTest {

    private void handleRequestTest(String header, String response) throws Exception {
        BufferedReader in = new BufferedReader(new StringReader(header));
        StringWriter output = new StringWriter();
        BufferedWriter out = new BufferedWriter(output);

        httpServer.handleRequest(in, out);
        assertTrue(output.toString().contains(response));
    }

    @Test
    public void requestHandlerTest(){
        assertEquals("request", httpServer.requestHandler("GET request"));
    }

    @Test
    public void fourOhFour() throws Exception {
        String header = "GET /foobar";
        String response = "404/Object Not Found";

        handleRequestTest(header, response);
    }

    @Test
    public void redirect() throws Exception {
        String header = "GET /redirect";
        String response = "307/Temporary Redirect";

        handleRequestTest(header, response);
    }

    @Test
    public void form() throws Exception {
        String header = "PUT /form";
        String response = "200/OK";

        handleRequestTest(header, response);
    }

    @Test
    public void echoBackQueryString() throws Exception {
        String header = "GET /some-script-url?variable_1=123459876&variable_2=some_value";
        String response = "200/OK";

        handleRequestTest(header, response);
    }

    @Test
    public void parameterDecode() throws Exception {
        String header = "GET /form?variable_1=Operators%20%3C%2C%20%3E%2C%20%3D%2C%20!%3D%3B%20%2B%2C%20-%2C%20*%2C%20%26%2C%20%40%2C%20%23%2C%20%24%2C%20%5B%2C%20%5D%3A%20%22is%20that%20all%22%3F";
        String response = "200/OK";

        handleRequestTest(header, response);
    }
}
