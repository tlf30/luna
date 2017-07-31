package io.luna.game.plugin;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.luna.LunaContext;
import io.luna.game.event.Event;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A manager for plugins that loads and managers each plugin.
 *
 * Used for jar based plugins
 *
 * @author Trevor Flynn {@literal <trevorflynn@liquidcrystalstudios.com>}
 */
public final class PluginManager {

    /**
     * The asynchronous logger.
     */
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * The context instance.
     */
    private final LunaContext context;
    
    /**
     * A list of all plugin instances.
     */
    private List<Plugin> plugins = new ArrayList<>();

    /**
     * The name of the plugins directory for searching for JAR files.
     */
    private final String PLUGIN_DIR = "plugins";
    /**
     * The name of the config directory for looking for config files
     */
    private final String CONFIG_DIR = "config";

    
    /**
     * Initilize the PluginManager 
     * 
     * @param context The context
     */
    public PluginManager(LunaContext context) {
        this.context = context;
    }

    /**
     * Loads each plugin from the Jar files found in the plugins dir.
     * 
     * @throws MalformedURLException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException 
     */
    public void load() throws MalformedURLException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        loadDefaultPlugins();
        File pluginDir = new File(PLUGIN_DIR);
        File[] files = pluginDir.listFiles();
        ArrayList<URL> pluginFiles = new ArrayList<>();
        for (File s : files) {
            if (s.getName().endsWith(".jar")) { //Get only jar files
                pluginFiles.add(s.toURI().toURL());
            }
        }
        URL[] importableMods = pluginFiles.toArray(new URL[pluginFiles.size()]);
        ClassLoader loader = new URLClassLoader(importableMods, ClassLoader.getSystemClassLoader()); //Build a classloader from the system classloader 
        for (URL jar : importableMods) { //Get each jar
            String[] classes = getClasses(jar); //Check class path
            for (String classpath : classes) {
                Class plugin = loader.loadClass(classpath);
                if (isPlugin(plugin)) { //If class is a plugin, add it
                    Plugin pluginInstance = (Plugin) plugin.newInstance();
                    LOGGER.info("Loaded plugin: " + pluginInstance.getName() + ":" + pluginInstance.getVersionID());
                    plugins.add(pluginInstance);
                }
            }
        }
    }
    
    /**
     * Load internal plugins inside Luna server jar.
     */
    private void loadDefaultPlugins() {
        
    }

    /**
     * Init plugins prior to server start
     * @throws IOException 
     */
    public void init() throws IOException {
        File configDir = new File(CONFIG_DIR);
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        for (Plugin plugin : plugins) {
            File configFile = new File(CONFIG_DIR + File.separator + plugin.getName() + ".json");
            if (!configFile.exists()) {
                configFile.createNewFile();
            }
            JsonParser parser = new JsonParser();
            JsonElement config = parser.parse(new BufferedReader(new FileReader(configFile)));
            
            
            try {
                plugin.init(context, configFile, config);
            } catch (Exception ex) {
                LOGGER.error("Could not init plugin " + plugin.getName());
                LOGGER.catching(ex);
            }
        }
    }

    /**
     * Start each plugin after server is started.
     */
    public void start() {
        for (Plugin plugin : plugins) {
            try {
                plugin.start();
            } catch (Exception ex) {
                LOGGER.error("Could not start plugin " + plugin.getName());
                LOGGER.catching(ex);
            }
        }
    }

    /**
     * Fire <code>Event</code> to each plugin
     * 
     * @param event 
     */
    public void fire(Event event) {
        for (Plugin plugin : plugins) {
            plugin.event(event);
        }
    }

    /**
     * Analyzes a class to check if it is an instance of <code>Plugin</code>
     * @param c The class to analyze
     * @return if the class is a plugin
     */
    private boolean isPlugin(Class c) {
        if (c.isSynthetic() || c.isAnnotation() || c.isEnum() || c.isInterface()
                || c.isLocalClass() || c.isMemberClass() || c.isPrimitive()
                || c.isAnonymousClass() || c.isArray()) {
            //We do not want any of these
            return false;
        }
        //Get the interfaces
        for (Class inf : c.getInterfaces()) {
            if (inf.isAssignableFrom(Plugin.class)) {
                //If it is an instance of plugin, we want it
                return true;
            }
        }
        return false;
    }

    /**
     * Analyze a Jar file and get every classpath from it.
     * 
     * @param url The URL of the JAR to analyze
     * @return A array of strings containing the classpaths of each class in the jar
     * @throws FileNotFoundException
     * @throws IOException 
     */
    private String[] getClasses(URL url) throws FileNotFoundException, IOException {
        ArrayList<String> classNames = new ArrayList<>();
        ZipInputStream zip = new ZipInputStream(new FileInputStream(url.getFile())); //Load the jar as a zip for processing
        for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) { //Get each entry in the zip
            if (!entry.isDirectory() && entry.getName().endsWith(".class")) { //If the entry is a class we need it
                String className = entry.getName().replace('/', '.'); // Fix format of path to make it a class path
                classNames.add(className.substring(0, className.length() - ".class".length())); //Take the '.class' off the end as it is not part of the class path
            }
        }
        return classNames.toArray(new String[classNames.size()]); //Return the class paths
    }
}
