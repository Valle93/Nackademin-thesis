package RedisStreamApi.workout;

import java.util.ArrayList;
import java.util.List;

public class testStringStreamer {

    public static void main(String[] args) {

        String searchInput = "Golf";

        searchInput = searchInput.toLowerCase();


        List<String> queries = new ArrayList<>();

        queries.add(searchInput);

        for (int i = 0; i < searchInput.length(); i++) {

            queries.add(searchInput.replace(searchInput.charAt(i), '?'));
        }

        for (int i = 0; i < searchInput.length() - 1; i++) {

            String tempString = searchInput;

            tempString = tempString.replace(searchInput.charAt(i), '?');
            tempString = tempString.replace(searchInput.charAt(i + 1), '?');

            queries.add(tempString);
        }

        for(int i = 0; i < searchInput.length() + 1; i++){

            char[] stringAsArray = new char[searchInput.length() + 1];

            byte doWeIndex = 0;

            for (int j = 0; j < stringAsArray.length; j++) {

                if(j == i){
                    stringAsArray[j] = '?';
                    doWeIndex++;

                }else{
                    stringAsArray[j] = searchInput.charAt(j - doWeIndex);
                }
            }

            queries.add(new String(stringAsArray));

        }

        for(int i = 0; i < searchInput.length() + 1; i++){

            char[] stringAsArray = new char[searchInput.length() + 1];

            byte doWeIndex = 0;

            for (int j = 0; j < stringAsArray.length; j++) {

                if(j == i){
                    stringAsArray[j] = '?';
                    doWeIndex++;

                }else{
                    stringAsArray[j] = searchInput.charAt(j - doWeIndex);
                }
            }

            queries.add(new String(stringAsArray));

        }

        queries.forEach(System.out::println);
    }
}
