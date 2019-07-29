package me.realized.duels.queue;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.queue.DQueue;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.kit.Kit;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Queue extends BaseButton implements DQueue {

    @Getter
    private final Kit kit;
    @Getter
    private final int bet;
    @Getter
    private final List<QueueEntry> players = new LinkedList<>();
    @Getter
    @Setter(value = AccessLevel.PACKAGE)
    private boolean removed;

    public Queue(final DuelsPlugin plugin, final Kit kit, final int bet) {
        super(plugin, ItemBuilder
            .of((plugin.getConfiguration().isInheritKitItemType() && kit != null) ? kit.getDisplayed().clone() : ItemBuilder.of(Material.DIAMOND_SWORD).build())
            .name(plugin.getLang().getMessage("GUI.queues.buttons.queue.name",
                "kit", kit != null ? kit.getName() : plugin.getLang().getMessage("none"), "bet_amount", bet, "players", 0))
            .lore(plugin.getLang().getMessage("GUI.queues.buttons.queue.lore",
                "kit", kit != null ? kit.getName() : plugin.getLang().getMessage("none"), "bet_amount", bet, "players", 0).split("\n"))
            .build());
        this.kit = kit;
        this.bet = bet;
    }

    @Override
    public boolean isInQueue(@Nonnull final Player player) {
        return players.stream().anyMatch(entry -> entry.getPlayer().equals(player));
    }

    @Nonnull
    @Override
    public List<Player> getQueuedPlayers() {
        return Collections.unmodifiableList(players.stream().sequential().map(QueueEntry::getPlayer).collect(Collectors.toList()));
    }

    void addPlayer(final QueueEntry entry) {
        players.add(entry);
        update();
        queueManager.getGui().calculatePages();
    }

    boolean removePlayer(final Player player) {
        if (players.removeIf(entry -> entry.getPlayer().equals(player))) {
            update();
            queueManager.getGui().calculatePages();
            return true;
        }

        return false;
    }

    boolean removeAll(final Set<QueueEntry> players) {
        if (this.players.removeAll(players)) {
            update();
            return true;
        }

        return false;
    }

    private void update() {
        setDisplayName(lang.getMessage("GUI.queues.buttons.queue.name",
            "kit", kit != null ? kit.getName() : lang.getMessage("none"), "bet_amount", bet, "players", players.size()));
        setLore(lang.getMessage("GUI.queues.buttons.queue.lore",
            "kit", kit != null ? kit.getName() : lang.getMessage("none"), "bet_amount", bet, "players", players.size()).split("\n"));
    }

    @Override
    public void onClick(final Player player) {
        queueManager.addToQueue(player, this);
    }

    @Override
    public String toString() {
        return (kit != null ? kit.getName() : "none") + " - $" + bet;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        final Queue queue = (Queue) other;
        return bet == queue.bet && Objects.equals(kit, queue.kit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kit, bet);
    }
}
