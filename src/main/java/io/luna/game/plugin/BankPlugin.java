package io.luna.game.plugin;

import io.luna.LunaContext;
import io.luna.game.event.Event;
import io.luna.game.event.impl.ButtonClickEvent;
import io.luna.game.event.impl.ObjectClickEvent.ObjectFirstClickEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

/**
 *
 * @author Trevor Flynn {@literal <trevorflynn@liquidcrystalstudios.com>}
 */
public class BankPlugin implements Plugin {

    private LunaContext context;
    private File config;

    @Override
    public String getName() {
        return "Banking";
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

    }

    @Override
    public void event(Event event) {
        if (event instanceof ObjectFirstClickEvent) {
            ObjectFirstClickEvent ofce = (ObjectFirstClickEvent) event;
            if (Arrays.asList(readConfig()).contains(new Integer(ofce.id()))) {
                ofce.plr().getBank().open();
            }
        } else if (event instanceof ButtonClickEvent) {
            ButtonClickEvent click = (ButtonClickEvent) event;
            if (click.id() == 5387) {
                click.plr().getAttributes().get("withdraw_as_note").set(false);
            } else if (click.id() == 5386) {
                click.plr().getAttributes().get("withdraw_as_note").set(true);
            }
        }
    }
    
    private Integer[] readConfig() {
        try {
            byte[] encoded = Files.readAllBytes(config.toPath());
            String[] lines = new String(encoded).replace("\r", "").split("\n");
            Integer[] banks = new Integer[lines.length];
            for (int i = 0; i < banks.length; i++) {
                banks[i] = Integer.decode(lines[i]);
            }
        } catch (IOException ex) {
            //
        }
        return new Integer[] {};
    }

}
