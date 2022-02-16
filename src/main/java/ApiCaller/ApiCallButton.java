package ApiCaller;

import lombok.Getter;

import javax.swing.*;

@Getter
public class ApiCallButton extends JButton {

    private ApiCall apiCall;

    public ApiCallButton(ApiCall apiCall){

        this.apiCall = apiCall;
    }

}
