package io.luna.game.plugin;

import io.luna.LunaContext;
import io.luna.game.event.Event;
import io.luna.game.event.impl.ButtonClickEvent;
import io.luna.game.event.impl.LoginEvent;
import io.luna.game.model.item.Item;
import io.luna.net.msg.out.ConfigMessageWriter;
import io.luna.net.msg.out.GameChatboxMessageWriter;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Trevor Flynn {@literal <trevorflynn@liquidcrystalstudios.com>}
 */
public class LoginPlugin implements Plugin {

    private LunaContext context;
    private File config;

    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, uuuu");
    private final List<Item> STARTER_ITEMS = new ArrayList<>();
    private final List<Item> STARTER_EQUIPMENT = new ArrayList<>();

    @Override
    public String getName() {
        return "Login";
    }

    @Override
    public int getVersionID() {
        return 0;
    }

    @Override
    public void init(LunaContext context, File config) {
        this.config = config;
        this.context = context;
        STARTER_ITEMS.add(new Item(995, 10000)); //Coins
        STARTER_ITEMS.add(new Item(556, 250)); //Air runes
        STARTER_ITEMS.add(new Item(555, 250)); //Water runes
        STARTER_ITEMS.add(new Item(554, 250)); //Fire runes
        STARTER_ITEMS.add(new Item(557, 250)); //Earth runes
        STARTER_ITEMS.add(new Item(558, 250)); //Mind runes
        STARTER_ITEMS.add(new Item(841)); //Shortbow
        STARTER_EQUIPMENT.add(new Item(1153)); //Iron full helm
        STARTER_EQUIPMENT.add(new Item(1115)); //Iron platebody
        STARTER_EQUIPMENT.add(new Item(1067)); //Iron platelegs
        STARTER_EQUIPMENT.add(new Item(1323)); //Iron scimitar
        STARTER_EQUIPMENT.add(new Item(1191)); //Iron kiteshield
        STARTER_EQUIPMENT.add(new Item(1731)); //Amulet of power
        STARTER_EQUIPMENT.add(new Item(4121)); //Iron boots
        STARTER_EQUIPMENT.add(new Item(1063)); //Leather vambraces
        STARTER_EQUIPMENT.add(new Item(2570)); //Ring of life
        STARTER_EQUIPMENT.add(new Item(1019)); //Black cape
        STARTER_EQUIPMENT.add(new Item(882, 750)); //Bronze arrows
    }

    @Override
    public void start() {

    }

    @Override
    public void event(Event event) {
        if (event instanceof LoginEvent) {
            //Check for first login
            if ((Boolean) ((LoginEvent) event).plr().getAttributes().get("first_login").get()) {
                ((LoginEvent) event).plr().queue(new GameChatboxMessageWriter("This is your first login. Enjoy your starter package!"));
                ((LoginEvent) event).plr().getInventory().addAll(STARTER_ITEMS);
                ((LoginEvent) event).plr().getEquipment().addAll(STARTER_EQUIPMENT);
                ((LoginEvent) event).plr().getAttributes().get("first_login").set(false);
            }
            //Check if muted
            String muted = ((LoginEvent) event).plr().getAttributes().get("unmute_date").get().toString();
            switch (muted) {
                case "n/a": /* Do nothing */ break;
                case "never": 
                    ((LoginEvent) event).plr().queue(new GameChatboxMessageWriter("You are permanently muted. It can only be overturned by an administrator."));
                    break;
                default:
                    ((LoginEvent) event).plr().queue(new GameChatboxMessageWriter("You are muted. You will be unmuted on" +  DATE_FORMATTER.format(LocalDate.parse(muted)) + "."));    
            }
            //Configure interface states
            ((LoginEvent) event).plr().queue(new ConfigMessageWriter(173, (((LoginEvent) event).plr().getWalkingQueue().isRunning() ? 1 : 0)));
        }
    }

}
