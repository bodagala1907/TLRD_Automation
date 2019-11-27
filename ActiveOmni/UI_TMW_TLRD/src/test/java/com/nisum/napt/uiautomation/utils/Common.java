package com.nisum.napt.uiautomation.utils;

import com.nisum.framework.utils.Utils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;

public class Common {

    private static Logger log = Logger.getLogger(Common.class);

    /**
     * Gets a resource file with a given name
     *
     * @param fName file name
     * @return resulting File
     */
    public static File getResourceFile(String fName) throws FileNotFoundException {
        log.debug("file name : " + fName);
        File resource;
        String path;
        // project data
        path = System.getProperty("user.dir") + "/src/test/resources/data/" + fName;
        resource = new File(path);
        if (resource.exists() && !resource.isDirectory()) {
            return resource;
        }
        // shared data
        String curDirPath = System.getProperty("user.dir");
        path = curDirPath
                .substring(0, curDirPath
                        .indexOf("TLRD_Automation") + 15) + "/Central/src/test/resources/data/" + fName;
        resource = new File(path);
        if (resource.exists() && !resource.isDirectory()) {
            return resource;
        }
        Assert.fail(fName + " file not found at " + resource.exists());
        return resource;
    }

    public static JSONObject getRandomAddress(String addressType) {
        JSONObject address = null;
        try {
            File addressFile = getResourceFile("addresses.json");
            String jsonTxt = Utils.readTextFile(addressFile);
            JSONObject json = new JSONObject(jsonTxt);
            JSONArray addresses = (JSONArray) json.get(addressType);
            Random rand = new Random();
            address = (JSONObject) addresses.get(rand.nextInt(addresses.length()));
            while (!address.getString("country").equalsIgnoreCase("United States")) {
                address = (JSONObject) addresses.get(rand.nextInt(addresses.length()));
            }
            if (address == null) {
                throw new Exception("Unable to find address matching given options");
            }
        } catch (Exception e) {
            Assert.fail("Unable to parse JSON: " + e);
            log.error("Unable to get random address: " + e.getMessage());
        }
        return address;
    }

}
