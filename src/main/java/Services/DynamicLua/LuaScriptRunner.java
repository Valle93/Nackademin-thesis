package Services.DynamicLua;

import Entities.AdvancedQueryCall;
import Entities.JedisStoreDB;
import Entities.Service;

import java.util.*;

public class LuaScriptRunner extends Thread implements Service {

    public Map<String, List<String>> responseMap;

    private int keyListSize;

    private int args;

    private String script;

    private Boolean whichList = true;

    private List<AdvancedQueryCall> one;

    private List<AdvancedQueryCall> two;

    private List<String> keysArray = new ArrayList<>();

    private List<String> argsArray = new ArrayList<>();

    private JedisStoreDB jedis;

    public LuaScriptRunner(JedisStoreDB jedis){

        responseMap = new HashMap<>();

        this.one = new ArrayList<>();

        this.two = new ArrayList<>();

        this.keysArray = new ArrayList<>();

        this.argsArray = new ArrayList<>();

        this.jedis = jedis;

    }

    public List<String> putApiCall (AdvancedQueryCall call){

        String id = generateID();

        call.setId(id);

        if(whichList){

            one.add(call);
        }else{

            two.add(call);
        }

        long then = System.currentTimeMillis();

        boolean b;

        do{

            long now = System.currentTimeMillis() - then;

            b = responseMap.containsKey(id);

            if(now > 3000){

                List<String> strings;

                strings = responseMap.get(id);

                for(String s : strings){

                    System.out.println(s);
                    System.out.println("hellu");
                }

                strings.add("Did not work");
                strings.add("id waiting for : " + id);

                return strings;

            }

        }while(!b);

        return responseMap.get(id);

    }

    private <T> T lua(String sha, int keys, String... args) {

        //noinspection unchecked
        return jedis.call(jedis -> (T) jedis.eval(sha, keys, args));
    }

    @Override
    public void run() {

        outerwhile: while(true) {

            doNothing(1000);

            List<AdvancedQueryCall> callsToWorkOn;

            if (whichList) {

                whichList = false;

                callsToWorkOn = one;

                one = new ArrayList<>();
            } else {

                whichList = true;

                callsToWorkOn = two;

                two = new ArrayList<>();

            }

            System.out.println("List Calls Size : " + callsToWorkOn.size());

            script = "local responses = {};\n\n";

            script += "local productids = {};\n\n";

            for (AdvancedQueryCall call : callsToWorkOn) {

                script += call.getLuaScript();
            }

            script += "return responses;";

           List<String> responses = lua(script, 0);


            String callid = null;
            try {
                callid = responses.get(0);
            } catch (Exception e) {

                continue outerwhile;
            }


            responses.remove(0);

            List<String> productsJSON = new ArrayList<>();

            int k = responses.size();

           outerfor: for(int i = 0; i < k; i++){

                if(responses.get(0).startsWith("res")){

                    responseMap.put(callid, productsJSON);
                    productsJSON = new ArrayList<>();
                    callid = responses.get(0);
                    responses.remove(0);
                    continue ;

                }

               productsJSON.add(responses.get(0));
               responses.remove(0);

           }

           responseMap.put(callid, productsJSON);

        }

    }

    public static String generateID() {

        return (key("response", randomUUID()));
    }

    static String key(String... parts) {
        return String.join(":", parts);
    }

    static String randomUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    void doNothing(long millis){

        long then = System.currentTimeMillis();

        do{

        }while(System.currentTimeMillis() - then < millis);

    }

}
