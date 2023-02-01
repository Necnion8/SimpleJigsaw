package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.panel;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;


public abstract class Panel {
    public static final Set<Panel> PANELS = new HashSet<>();
    public static Plugin OWNER;

    private final EventListener listener = new EventListener();
    private Inventory inventory;
    private final Player player;
//    private final Map<ItemStack, PanelItem> cachedItems = Maps.newLinkedHashMap();
    private PanelItem[] cachedItems;
    private final ItemStack backgroundItem;
    private MessageHandler messageHandler;
    private MessageListener messageListener;
    private int lastInvSize;
    private String lastInvTitle;
    private InventoryType lastInvType = InventoryType.CHEST;

    private Panel backPanel;
    private @Nullable Boolean openParentWhenClosing = null;


    public Panel(Player player, int size, String title, @Nullable ItemStack background) {
//        this.inventory = Bukkit.createInventory(null, size, invTitle);
        this.player = player;
        backgroundItem = background;
        this.lastInvSize = size;
        this.lastInvTitle = title;
    }

    public Panel(Player player, InventoryType invType, int size, String title, @Nullable ItemStack background) {
//        this.inventory = Bukkit.createInventory(null, size, invTitle);
        this.player = player;
        backgroundItem = background;
        this.lastInvType = invType;
        this.lastInvSize = size;
        this.lastInvTitle = title;
    }

    public Panel(Player player, int size, String title) {
        this(player, size, title, PanelItem.createBlankItem().getItemStack());
    }


    public void open() {
        placeItems(build());

        HandlerList.unregisterAll(listener);
        Bukkit.getPluginManager().registerEvents(listener, OWNER);
        if (!inventory.getViewers().contains(player)) {
            player.openInventory(inventory);
        }
        PANELS.add(this);
    }

    public void open(@Nullable Panel parentPanel) {
        backPanel = parentPanel;
        this.open();
    }

    public void update() {
        placeItems(build());
    }

    public void refresh() {
        if (cachedItems != null)
            placeItems(cachedItems);
    }

    public void destroy(boolean close) {
        HandlerList.unregisterAll(listener);

        if (inventory != null && inventory.equals(player.getOpenInventory().getTopInventory()) && close) {
            player.closeInventory();
        }

        PANELS.remove(this);
    }

    public void destroy() {
        destroy(true);
    }

    public static void destroyAll() {
        new HashSet<>(PANELS).forEach(Panel::destroy);
    }


    public Inventory getInventory() {
        return inventory;
    }

    public Player getPlayer() {
        return player;
    }


    public void placeItems(PanelItem[] items) {
//        cachedItems.clear();
        cachedItems = items;
//        if (inventory != null)
//            inventory.clear();

        boolean reopen = false;
        int size = getSize();
        String title = getTitle();
        InventoryType invType = getInventoryType();
        if (inventory == null || lastInvSize != size || !lastInvTitle.equals(title) || !lastInvType.equals(invType)) {
            reopen = inventory != null;
            if (InventoryType.CHEST.equals(lastInvType)) {
                inventory = Bukkit.createInventory(null, (size > 0) ? size : 9, (title != null) ? title : "");
            } else {
                inventory = Bukkit.createInventory(null, lastInvType, (title != null) ? title : "");
            }
            lastInvSize = size;
            lastInvTitle = title;
        }

        int index = 0;
        for (PanelItem item : items) {
            ItemStack itemStack = null;

            if (item != null)
                itemStack = item.getItemBuilder().build(player);

//            if (itemStack != null) {
////                cachedItems.put(itemStack, item);
//            } else if (backgroundItem != null) {
            if (itemStack == null && backgroundItem != null) {
                itemStack = backgroundItem.clone();
            }

            inventory.setItem(index, itemStack);
            index++;
        }
        int emptySlots = inventory.getSize() - items.length;
        for (int i = 0; i < emptySlots; i++) {
            inventory.setItem(items.length + i + 1, Optional.ofNullable(backgroundItem).map(ItemStack::clone).orElse(null));
        }

        if (reopen)
            player.openInventory(inventory);

    }

//    protected PanelItem selectPanelItem(ItemStack item) {
//        for (Map.Entry<ItemStack, PanelItem> e : cachedItems) {
//            if (e.getKey().equals(item))
//                return e.getValue();
//        }
////        return cachedItems.get(item);
//        return null;
//    }

    protected PanelItem selectPanelItem(int slotId) {
        if (cachedItems == null || cachedItems.length <= slotId)
            return null;
        return cachedItems[slotId];
    }

    public void setMessageHandler(MessageHandler listener) {
        if (messageListener != null) {
            HandlerList.unregisterAll(messageListener);
        }

        messageHandler = listener;
        if (listener != null) {
            messageListener = new MessageListener();
            Bukkit.getPluginManager().registerEvents(messageListener, OWNER);
        }
    }

    public int getSize() {
        return lastInvSize;
    }

    public String getTitle() {
        return lastInvTitle;
    }

    public InventoryType getInventoryType() {
        return lastInvType;
    }

    public Panel setBackPanel(Panel backPanel) {
        this.backPanel = backPanel;
        return this;
    }

    public Panel getBackPanel() {
        return backPanel;
    }

    public PanelItem createBackButton() {
        return PanelItem.createItem(Material.BARRIER, "")
                .setClickListener((e, p) -> {
                    Panel backPanel = getBackPanel();
                    if (backPanel == null) {
                        destroy(true);
                    } else {
                        backPanel.open();
                        destroy(false);
                        playClickSound(p);
                    }
                }).setItemBuilder((p) -> {
                    Material icon;
                    String name;
                    Panel back = getBackPanel();
                    if (back != null) {
                        icon = Material.ACACIA_DOOR;
                        name = ChatColor.GOLD + ChatColor.stripColor(back.getTitle()) + "ページに戻る";
                    } else {
                        icon = Material.OAK_DOOR;
                        name = ChatColor.RED + "閉じる";
                    }
                    return PanelItem.createItem(icon, name).getItemStack();
                });
    }

    public void playClickSound(Player player) {
//        player.playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, .75f, 2f);
    }

    public void playClickSound(Player player, Sound sound, float pitch) {
//        player.playSound(player.getLocation(), sound, .75f, pitch);
    }

    public void playClickSound(Sound sound, float pitch) {
//        player.playSound(player.getLocation(), sound, .75f, pitch);
    }

    public boolean isOpenParentWhenClosing() {
        return (openParentWhenClosing != null) ? openParentWhenClosing : backPanel != null && backPanel.isOpenParentWhenClosing();
    }

    public void setOpenParentWhenClosing(@Nullable Boolean open) {
        this.openParentWhenClosing = open;
    }

    abstract public PanelItem[] build();

    public boolean onClick(InventoryClickEvent event) {
        return false;
    }

    public void onEvent(InventoryClickEvent event) {}

    public void onEvent(InventoryCloseEvent event) {}

    public void onEvent(InventoryDragEvent event) {}



    private class EventListener implements Listener {
        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            if (event.getPlayer().equals(player))
                destroy();
        }

        @EventHandler(priority = EventPriority.HIGH)
        public void onClose(InventoryCloseEvent event) {
            onEvent(event);

            if (!inventory.equals(event.getInventory()))
                return;

            destroy(false);

            if (isOpenParentWhenClosing() && backPanel != null) {
                Bukkit.getScheduler().runTaskLater(OWNER, () -> {  // hacky X(
                    Inventory inv = event.getPlayer().getOpenInventory().getTopInventory();
                    if (InventoryType.CRAFTING.equals(inv.getType()))  // non opened
                        backPanel.open();
                }, 0);
            }

        }

        @EventHandler(priority = EventPriority.HIGH)
        public void onDrag(InventoryDragEvent event) {
            onEvent(event);
            if (!inventory.equals(event.getInventory()))
                return;

            for (Integer slot : event.getRawSlots()) {
                if (inventory.getSize() > slot) {
                    event.setCancelled(true);
                    event.setResult(Event.Result.DENY);
                    return;
                }
            }
        }

        @EventHandler(priority = EventPriority.HIGH)
        public void onClick(InventoryClickEvent event) {
            onEvent(event);
            if (!inventory.equals(event.getInventory()))
                return;

            if (InventoryAction.COLLECT_TO_CURSOR.equals(event.getAction()) && event.getCursor() != null) {
                if (Stream.of(inventory.getContents())
                        .anyMatch(i -> event.getCursor().isSimilar(i))) {
                    event.setCancelled(true);
                    event.setResult(Event.Result.DENY);
                    return;
                }
            } else if (InventoryAction.MOVE_TO_OTHER_INVENTORY.equals(event.getAction())) {
                event.setCancelled(true);
                event.setResult(Event.Result.DENY);
            }

            if (!inventory.equals(event.getClickedInventory()))
                return;

            event.setCancelled(true);
            event.setResult(Event.Result.DENY);

            if (!Panel.this.onClick(event)) {
                switch (event.getAction()) {
                    case PICKUP_ALL:
                    case PICKUP_HALF:
                    case PICKUP_ONE:
                    case PICKUP_SOME:
                    case MOVE_TO_OTHER_INVENTORY:
                        break;
                    default:
                        return;
                }

                PanelItem selected = selectPanelItem(event.getSlot());

                if (selected != null)
                    selected.getClickListener().click(event, player);
            }

        }

    }

    private class MessageListener implements Listener {
        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            if (event.getPlayer().equals(getPlayer()))
                setMessageHandler(null);
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onChat(AsyncPlayerChatEvent event) {
            if (messageHandler != null && event.getPlayer().equals(getPlayer())) {
                event.setCancelled(true);

                Bukkit.getScheduler().runTask(OWNER, () -> {
                    if (messageHandler.onMessage(event.getMessage()))
                        setMessageHandler(null);
                });
            }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onCommand(PlayerCommandPreprocessEvent event) {
            if (messageHandler != null && event.getPlayer().equals(getPlayer())) {
                event.setCancelled(true);
                if (!messageHandler.onMessage(event.getMessage()))
                    return;
                setMessageHandler(null);
            }
        }

    }



    public interface MessageHandler {
        boolean onMessage(String message);
    }


}
