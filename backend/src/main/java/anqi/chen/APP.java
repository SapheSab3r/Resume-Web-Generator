package anqi.chen;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import anqi.chen.framework.DataPlugin;
import anqi.chen.framework.ResumeFramework;
import org.json.JSONObject;
import fi.iki.elonen.NanoHTTPD;

public class APP extends NanoHTTPD {
    static final int PORT = 8080;
    private ResumeFramework framework;
    private HashMap<String, DataPlugin> plugins;

    public static void main(String[] args) {
        try {
            new APP();
        } catch (IOException ioe) {
            System.err.println("Couldn't start server:\n" + ioe);
        }
    }

    /**
     * Start the server at :8080 port.
     * 
     * @throws IOException
     */
    public APP() throws IOException {
        super(PORT);
        framework = new ResumeFramework();
        plugins = loadPlugins();
        for (DataPlugin p : plugins.values()) {
            framework.registerPlugin(p);
        }

        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("Plugins: " + plugins);
        System.out.println("Plugin Names: " + plugins.keySet()); // Print the keys (plugin names)
        System.out.println("\nRunning! Point your browsers to http://localhost:8080/ \n");

    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Map<String, String> params = session.getParms();

        // System.out.println("Request URI: " + uri);
        // System.out.println("Request Parameters: " + params);

        JSONObject res = new JSONObject();
        // System.out.println(params.get("pluginName"));
        // System.out.println(params.get("resumePath"));

        // Handling requests to /getDataPlugin
        if (uri.equals("/getDataPlugin")) {
            try {
                res.put("plugins", plugins.keySet().toArray());
            } catch (Exception e) {
                System.err.println("Error putting data: " + e.getMessage());
            }
        }
        // Handling requests to /parseResume
        else if (uri.equals("/parseResume")) {
            // check if the params are present
            if (!params.containsKey("pluginName") || !params.containsKey("resumePath")) {
                // set error
                res.put("status", 0);
                res.put("resume", new JSONObject());
                res.put("message", "Require two parameters: pluginName and resumePath");
            }
        }
        // Check if the params exist
        else if (params.get("pluginName") == null || !this.plugins.containsKey(params.get("pluginName"))) {
            res.put("status", 0);
            res.put("resume", new JSONObject());
            res.put("message", "there is no such data plugin: " + params.get("pluginName"));
        } else {
            // Register the specified plugin and attempt to process the resume
            framework.registerPlugin(plugins.get(params.get("pluginName")));
            try {
                framework.setResumeDataSrc(params.get("resumePath"));
                res.put("status", 1);
                res.put("resume", framework.getResumeDataInJsonObject());
                res.put("message", "success");
            } catch (IOException e) {
                res.put("message", e.getMessage());
                res.put("status", 0);
                res.put("resume", new JSONObject());
                System.out.println("opps");
                res.put("message", "Failed to parse your resume. Please refer to the examples." + e.getMessage());
            } catch (Exception e) {
                res.put("status", 0);
                System.out.println("uh?");
                res.put("resume", new JSONObject());
            }
        }

        // /getDataPlugins {"plugins": ["xx" , "xx"]}
        // /parseResume POST para1 = resumePath para2 = pluginName Return Json
        // {"status": 0/1, "resume":{}, message:""} (0 failed, 1 success)
        System.out.println(res.toString());
        return newFixedLengthResponse(res.toString());
    }

    /**
     * Load plugins listed in META-INF/services/...
     * 
     * @return List of instantiated plugins
     */
    private HashMap<String, DataPlugin> loadPlugins() {
        ServiceLoader<DataPlugin> plugins = ServiceLoader.load(DataPlugin.class);
        HashMap<String, DataPlugin> result = new HashMap<>();
        for (DataPlugin plugin : plugins) {
            System.out.println("Loaded plugin " + plugin.getPluginName());
            result.put(plugin.getPluginName(), plugin);
        }
        return result;
    }
}
