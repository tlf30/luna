package io.luna.game.plugin;

import io.luna.LunaContext;
import io.luna.game.event.Event;
import io.luna.game.event.impl.ServerLaunchEvent;
import io.luna.game.model.mobile.Player;
import io.luna.game.model.mobile.PlayerRights;
import io.luna.game.task.Task;
import io.luna.net.msg.out.GameChatboxMessageWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;
import java.util.function.Predicate;

/**
 * The announcements plugin.
 *
 *
 * @author Trevor Flynn {@literal <trevorflynn@liquidcrystalstudios.com>}
 */
public class AnnouncementsPlugin implements Plugin {

    private LunaContext context;
    private File config;

    @Override
    public String getName() {
        return "Announcements";
    }

    @Override
    public int getVersionID() {
        return 0;
    }

    @Override
    public void init(LunaContext context, File config) {
        this.config = config;
        this.context = context;
    }

    @Override
    public void start() {
        //Nothing to do
    }

    @Override
    public void event(Event event) {
        if (event instanceof ServerLaunchEvent) {
            ServerLaunchEvent sle = (ServerLaunchEvent) event;
            Task task = new Task(true, 30) {
                @Override
                protected void execute() {
                    String[] lines = readConfig();
                    Random r = new Random();
                    String line = lines[r.nextInt(lines.length)];
                    context.getWorld().getPlayers().findAll(new Predicate() {
                        @Override
                        public boolean test(Object t) {
                            Player p = (Player) t;
                            return p.getRights() == PlayerRights.ADMINISTRATOR;
                        }
                    }).forEach(player -> ((Player) player).queue(new GameChatboxMessageWriter(line)));
                }
            };
            context.getWorld().schedule(task);
        }
    }

    private String[] readConfig() {
        try {
            byte[] encoded = Files.readAllBytes(config.toPath());
            return new String(encoded).replace("\r", "").split("\n");
        } catch (IOException ex) {
            //
        }
        return new String[] {};
    }

}
