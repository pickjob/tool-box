package app.util;

import app.App;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: pickjob@126.com
 * @date: 2020-08-30
 **/
public class Constants {
    public static List<String> loadStyleSheets() {
        List<String> result = new ArrayList<>();
        result.add(App.class.getResource("/css/global.css").toExternalForm());
        return result;
    }
}
