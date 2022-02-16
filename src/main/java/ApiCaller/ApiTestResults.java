package ApiCaller;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiTestResults {

    private int[] millis0to50;
    private int[] millis0to200;
    private String api_url;
    private int numberOfThreads;
    private int callsPerThread;
    private int totaltime;

}
