package com.itwookie.discordbot;

import com.itwookie.discordbot.logscanning.LogManager;
import de.dosmike.sponge.oreapi.OreApiV2;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.imageio.ImageIO;
import javax.security.auth.login.LoginException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Executable {

    static JDA jda;
    static ScheduledExecutorService exec;
    static OreApiV2 ore;
    static boolean running=true;
    private static TrayIcon icon;
    public static void postNotification(String caption, String message) {
        if (!SystemTray.isSupported()) return;
        icon.displayMessage(caption, message, TrayIcon.MessageType.NONE);
    }
    public static void queue(Runnable r) {
        exec.execute(r);
    }

    static Map<String,String> botProperties = new HashMap<>();

    public static void main(String[] args) {

        redirectIO();

        System.out.println("Setting up SysTray");
        setupTray();
        System.out.println("Loading properties");
        if (!loadProperties()) return;
        System.out.println("Starting JDA");
        setupJDA(botProperties.getOrDefault("jdatoken",""));

        System.out.println("Setting up Ore");
        exec = Executors.newScheduledThreadPool(8);
        ore = new OreApiV2();

        System.out.println("Register Log Patterns");
        LogAnalyzer.register();
        System.out.println("Register Commands");
        Commands.register();

        System.out.println("Done!");
        updateTrayIcon();
        while (running) try { Thread.sleep(1000); } catch (Exception ignore) {}
        System.exit(0);
    }

    static void test() {
        System.out.println("'Test' has currently no function");
    }

    private static boolean terminated=false;
    private static void redirectIO() {
        try {
            //prepare pipes
            PipedOutputStream cout_pos = new PipedOutputStream();
            PipedInputStream cout_pis = new PipedInputStream(cout_pos);
            PipedOutputStream cerr_pos = new PipedOutputStream();
            PipedInputStream cerr_pis = new PipedInputStream(cerr_pos);

            //prepare streams and redirect system.out into the pipe
            final PrintStream logfileOut = new PrintStream("latest.log");
            final PrintStream systemOut = System.out;
            final PrintStream systemErr = System.err;
            System.setOut(new PrintStream(cout_pos,true));
            System.setErr(new PrintStream(cerr_pos,true));

            //start a thread that permanently reads from the pipes and
            // writes into the log and original sout
            Thread outputRestreamer = new Thread() {
                byte[] buffer = new byte[1024]; int read;
                @Override
                public void run() {
                    try {
                        while (!terminated) {
                            while (cout_pis.available() > 0) {
                                read = cout_pis.read(buffer);
                                logfileOut.write(buffer, 0, read);
                                systemOut.write(buffer, 0, read);
                            }
                            while (cerr_pis.available() > 0) {
                                read = cerr_pis.read(buffer);
                                logfileOut.write(buffer, 0, read);
                                systemErr.write(buffer, 0, read);
                            }
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ignore) { }
                        }
                    } catch (IOException ignore) { }
                    finally {
                        logfileOut.flush();
                        logfileOut.close();
                    }
                }
            };
            outputRestreamer.setName("Output Restreamer");
            outputRestreamer.start();

            //Hook termination to interrupt the outstreamer
            Runtime.getRuntime().addShutdownHook(new Thread(() -> terminated = true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static boolean loadProperties() {
        Path propertyFile = Paths.get("bot.properties");
        try {
            if (Files.exists(propertyFile)) {
                BufferedReader br = Files.newBufferedReader(propertyFile);
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty() || line.startsWith("#")) continue;
                    int o1 = line.indexOf(':'), o2 = line.indexOf('=');
                    if (o1 < 0 || (o1 > o2 && o2 >= 0)) o1 = o2;
                    if (o1 >= 0) {
                        botProperties.put(line.substring(0, o1), line.substring(o1 + 1));
                    }
                }
                br.close();
                return true;
            } else {
                BufferedWriter bw = Files.newBufferedWriter(propertyFile);
                bw.write("# please add you java bot token here:\njdatoken=\n");
                bw.flush();
                bw.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read properties", e);
        }
        return false;
    }

    private static void setupTray() {
        if (!SystemTray.isSupported()) return;
        try {
            URL icon_in = Executable.class.getResource("/assets/icon.png");
            Image img = ImageIO.read(icon_in).getScaledInstance(16,16,Image.SCALE_FAST);
            BufferedImage gsimg = new BufferedImage(16,16,BufferedImage.TYPE_BYTE_GRAY);
            gsimg.getGraphics().drawImage(img, 0,0,null);
            icon = new TrayIcon(gsimg);
            PopupMenu popupMenu = new PopupMenu();

            MenuItem test = new MenuItem("Test");
            test.addActionListener(e->exec.execute(Executable::test));
            popupMenu.add(test);
            popupMenu.addSeparator();

            MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(e->{
                System.out.println("Terminating");
                if (jda!=null)jda.shutdownNow();
                if (exec!=null)exec.shutdownNow();
                if (ore!=null)try { ore.close(); }catch (Exception ignore) { }
                SystemTray.getSystemTray().remove(icon);
                running = false;
            });
            popupMenu.add(exitItem);
            icon.setPopupMenu(popupMenu);
            icon.setToolTip("DosMike's Discord Bot");
            SystemTray.getSystemTray().add(icon);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
    private static void updateTrayIcon() {
        if (!SystemTray.isSupported()) return;
        try {
            URL icon_in = Executable.class.getResource("/assets/icon.png");
            Image img = ImageIO.read(icon_in).getScaledInstance(16,16,Image.SCALE_FAST);
            icon.setImage(img);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
    private static void setupJDA(String token) {
        try {
            jda = JDABuilder.createDefault(token,
                    GatewayIntent.GUILD_EMOJIS,
                    GatewayIntent.GUILD_EMOJIS,
                    GatewayIntent.GUILD_INVITES,
                    GatewayIntent.GUILD_MEMBERS,
                    GatewayIntent.GUILD_MESSAGES,
                    GatewayIntent.GUILD_MESSAGE_REACTIONS,
                    GatewayIntent.GUILD_MESSAGE_TYPING,
                    GatewayIntent.GUILD_PRESENCES
            )       .disableCache(CacheFlag.VOICE_STATE)
                    .setEventManager(new AnnotatedEventManager())
                    .addEventListeners(new EventListener())
                    .build();
            jda.awaitReady();
        } catch (LoginException|InterruptedException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}
