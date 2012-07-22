package com.md_5.bot.mc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import lombok.Getter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

public class Main {

    private static final String title = (Main.class.getPackage().getImplementationTitle() == null) ? "MC-Bot"
            : Main.class.getPackage().getImplementationTitle();
    private static final String vendor = (Main.class.getPackage().getImplementationVendor() == null) ? "md_5"
            : Main.class.getPackage().getImplementationVendor();
    private static final String version = (Main.class.getPackage().getImplementationVersion() == null) ? "Unknown"
            : Main.class.getPackage().getImplementationVersion();
    @Getter
    private static final Collection<Connection> activeConnections = new ArrayList<Connection>();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        System.out.println(title + " version " + version + " by " + vendor);

        Class.forName("net.minecraft.server.AchievementList");

        if (args.length != 1) {
            System.out.println("Incorrect arguments, usage is: java -jar " + title + ".jar <script.js>");
            return;
        }

        String scriptName = args[0];
        File file = new File(scriptName);
        if (!file.exists()) {
            System.out.println("Error: The specified file, " + file.getName() + " does not exist");
            return;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        StringBuilder script = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            script.append(line);
            script.append("\n");
        }
        reader.close();
        System.out.println("Read script: " + scriptName);

        Context cx = Context.enter();
        ScriptableObject scriptable = new ImporterTopLevel(cx);
        Scriptable scope = cx.initStandardObjects(scriptable);

        scope.put("out", scope, Context.javaToJS(System.out, scope));

        System.out.println("Executing script");

        Object result = cx.evaluateString(scope, script.toString(), scriptName, 1, null);

        if (!(result instanceof Undefined)) {
            System.out.println("Script returned: " + Context.toString(result));
        }

        System.out.println("Execution finished, waiting for all connections to terminate");
        while (!Main.activeConnections.isEmpty()) {
            Thread.sleep(1500);
        }
        System.out.println("Execution finished");
    }
}
