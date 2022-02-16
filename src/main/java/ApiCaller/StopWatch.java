package ApiCaller;

public class StopWatch {

    private boolean hasTheStopWatchBeenStarted;
    private Long startTimer;

    public StopWatch(){
        hasTheStopWatchBeenStarted = false;
    }

    public void startTimer(){

        startTimer = System.currentTimeMillis();
        hasTheStopWatchBeenStarted = true;

    }

    public long stopTimer(){

        if(!hasTheStopWatchBeenStarted)
            System.out.println("StopWatch not started");

        hasTheStopWatchBeenStarted = false;

        return System.currentTimeMillis() - startTimer;

    }
}
