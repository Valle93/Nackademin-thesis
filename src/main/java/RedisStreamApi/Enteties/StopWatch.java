package RedisStreamApi.Enteties;

public class StopWatch {

    private boolean hasTheStopWatchBeenStarted;
    private Long startTimer;
    private Long stopTimer;
    private String typeInWhatYouAreMeasuring;

    public StopWatch(){
        hasTheStopWatchBeenStarted = false;
    }

    public void startTimer(String typeInWhatYouAreMeasuring){

        startTimer = System.currentTimeMillis();
        hasTheStopWatchBeenStarted = true;
        this.typeInWhatYouAreMeasuring = typeInWhatYouAreMeasuring;
    }

    public void stopTimer(){

        if(!hasTheStopWatchBeenStarted)
            System.out.println("StopWatch not started");

        this.stopTimer = System.currentTimeMillis() - startTimer;

        hasTheStopWatchBeenStarted = false;

        System.out.println("The time it took to " + typeInWhatYouAreMeasuring + " was: " + stopTimer + " milliseconds");
    }
}
