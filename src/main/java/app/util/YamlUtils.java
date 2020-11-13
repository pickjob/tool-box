package app.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author ws@yuan-mai.com
 * @Date 2020-11-11
 */
public class YamlUtils {
    private static final Logger logger = LogManager.getLogger(YamlUtils.class);

    public static Map<String, String> covertYamlToProperties(File file) {
        Map<String, String> result = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            Map map = mapper.readValue(file, Map.class);
            logger.info("configMap: {}", map);
            for (Object key : map.keySet()) {
                flat(key, map.get(key), "", result);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("configMap: {}", result);
        return result;
    }

    private static void flat(Object key, Object val, String prefix, Map<String, String> result) {
        if (val instanceof Map map) {
            for (Object k : map.keySet()) {
                if (StringUtils.isBlank(prefix)) {
                    flat(k, map.get(k), key + "", result);
                } else {
                    flat(k, map.get(k), prefix + "." + key, result);
                }
            }
        } else if (val instanceof List list) {
            for (int i = 0; i < list.size(); i++) {
                flat(key + "[" + i + "]", list.get(i), prefix, result);
            }
        } else {
            if (StringUtils.isBlank(prefix)) {
                result.put(key + "", val + "");
            } else {
                result.put(prefix + "." + key, val + "");
            }
        }
    }
}
