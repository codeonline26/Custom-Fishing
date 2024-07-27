/*
 *  Copyright (C) <2024> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.momirealms.customfishing.bukkit;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.event.CustomFishingReloadEvent;
import net.momirealms.customfishing.api.mechanic.MechanicType;
import net.momirealms.customfishing.api.mechanic.config.ConfigManager;
import net.momirealms.customfishing.api.mechanic.misc.cooldown.CoolDownManager;
import net.momirealms.customfishing.api.mechanic.misc.placeholder.BukkitPlaceholderManager;
import net.momirealms.customfishing.api.util.EventUtils;
import net.momirealms.customfishing.bukkit.action.BukkitActionManager;
import net.momirealms.customfishing.bukkit.bag.BukkitBagManager;
import net.momirealms.customfishing.bukkit.block.BukkitBlockManager;
import net.momirealms.customfishing.bukkit.command.BukkitCommandManager;
import net.momirealms.customfishing.bukkit.competition.BukkitCompetitionManager;
import net.momirealms.customfishing.bukkit.config.BukkitConfigManager;
import net.momirealms.customfishing.bukkit.effect.BukkitEffectManager;
import net.momirealms.customfishing.bukkit.entity.BukkitEntityManager;
import net.momirealms.customfishing.bukkit.event.BukkitEventManager;
import net.momirealms.customfishing.bukkit.fishing.BukkitFishingManager;
import net.momirealms.customfishing.bukkit.game.BukkitGameManager;
import net.momirealms.customfishing.bukkit.hook.BukkitHookManager;
import net.momirealms.customfishing.bukkit.integration.BukkitIntegrationManager;
import net.momirealms.customfishing.bukkit.item.BukkitItemManager;
import net.momirealms.customfishing.bukkit.loot.BukkitLootManager;
import net.momirealms.customfishing.bukkit.market.BukkitMarketManager;
import net.momirealms.customfishing.bukkit.migration.Migration;
import net.momirealms.customfishing.bukkit.requirement.BukkitRequirementManager;
import net.momirealms.customfishing.bukkit.scheduler.BukkitSchedulerAdapter;
import net.momirealms.customfishing.bukkit.sender.BukkitSenderFactory;
import net.momirealms.customfishing.bukkit.statistic.BukkitStatisticsManager;
import net.momirealms.customfishing.bukkit.storage.BukkitStorageManager;
import net.momirealms.customfishing.bukkit.totem.BukkitTotemManager;
import net.momirealms.customfishing.common.dependency.Dependency;
import net.momirealms.customfishing.common.dependency.DependencyManagerImpl;
import net.momirealms.customfishing.common.helper.VersionHelper;
import net.momirealms.customfishing.common.locale.TranslationManager;
import net.momirealms.customfishing.common.plugin.classpath.ClassPathAppender;
import net.momirealms.customfishing.common.plugin.classpath.ReflectionClassPathAppender;
import net.momirealms.customfishing.common.plugin.logging.JavaPluginLogger;
import net.momirealms.customfishing.common.plugin.logging.PluginLogger;
import net.momirealms.sparrow.heart.SparrowHeart;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public class BukkitCustomFishingPluginImpl extends BukkitCustomFishingPlugin {

    private final ClassPathAppender classPathAppender;
    private final PluginLogger logger;
    private BukkitCommandManager commandManager;
    private Consumer<Object> debugger;

    public BukkitCustomFishingPluginImpl(Plugin boostrap) {
        super(boostrap);
        VersionHelper.init(getServerVersion());
        this.scheduler = new BukkitSchedulerAdapter(this);
        this.classPathAppender = new ReflectionClassPathAppender(this);
        this.logger = new JavaPluginLogger(getBoostrap().getLogger());
        this.dependencyManager = new DependencyManagerImpl(this);
    }

    @Override
    public void load() {
        this.dependencyManager.loadDependencies(
                List.of(
                        Dependency.BOOSTED_YAML,
                        Dependency.BSTATS_BASE, Dependency.BSTATS_BUKKIT,
                        Dependency.CAFFEINE,
                        Dependency.GEANTY_REF,
                        Dependency.CLOUD_CORE, Dependency.CLOUD_SERVICES, Dependency.CLOUD_BUKKIT, Dependency.CLOUD_PAPER, Dependency.CLOUD_BRIGADIER, Dependency.CLOUD_MINECRAFT_EXTRAS,
                        Dependency.GSON,
                        Dependency.COMMONS_POOL_2,
                        Dependency.JEDIS,
                        Dependency.EXP4J,
                        Dependency.MYSQL_DRIVER, Dependency.MARIADB_DRIVER,
                        Dependency.SQLITE_DRIVER, Dependency.SLF4J_API, Dependency.SLF4J_SIMPLE,
                        Dependency.H2_DRIVER,
                        Dependency.MONGODB_DRIVER_CORE, Dependency.MONGODB_DRIVER_SYNC, Dependency.MONGODB_DRIVER_BSON,
                        Dependency.HIKARI_CP,
                        Dependency.LZ4
                )
        );
    }

    @Override
    public void enable() {
        SparrowHeart.getInstance();
        this.configManager = new BukkitConfigManager(this);
        new Migration(this).start();
        this.eventManager = new BukkitEventManager(this);
        this.requirementManager = new BukkitRequirementManager(this);
        this.actionManager = new BukkitActionManager(this);
        this.senderFactory = new BukkitSenderFactory(this);
        this.placeholderManager = new BukkitPlaceholderManager(this);
        this.itemManager = new BukkitItemManager(this);
        this.competitionManager = new BukkitCompetitionManager(this);
        this.marketManager = new BukkitMarketManager(this);
        this.storageManager = new BukkitStorageManager(this);
        this.lootManager = new BukkitLootManager(this);
        this.coolDownManager = new CoolDownManager(this);
        this.entityManager = new BukkitEntityManager(this);
        this.blockManager = new BukkitBlockManager(this);
        this.statisticsManager = new BukkitStatisticsManager(this);
        this.effectManager = new BukkitEffectManager(this);
        this.hookManager = new BukkitHookManager(this);
        this.fishingManager = new BukkitFishingManager(this);
        this.bagManager = new BukkitBagManager(this);
        this.totemManager = new BukkitTotemManager(this);
        this.translationManager = new TranslationManager(this);
        this.integrationManager = new BukkitIntegrationManager(this);
        this.gameManager = new BukkitGameManager(this);
        this.commandManager = new BukkitCommandManager(this);
        this.commandManager.registerDefaultFeatures();

        this.reload();
        if (ConfigManager.metrics()) new Metrics((JavaPlugin) getBoostrap(), 16648);
        if (ConfigManager.checkUpdate()) {
            VersionHelper.UPDATE_CHECKER.apply(this).thenAccept(result -> {
                if (!result) this.getPluginLogger().info("You are using the latest version.");
                else this.getPluginLogger().warn("Update is available: https://polymart.org/resource/2723");
            });
        }
    }

    @Override
    public void reload() {
        MechanicType.reset();

        this.itemManager.unload();
        this.eventManager.unload();
        this.entityManager.unload();
        this.lootManager.unload();
        this.blockManager.unload();
        this.effectManager.unload();
        this.hookManager.unload();
        this.competitionManager.unload();
        this.totemManager.unload();

        this.actionManager.reload();
        this.requirementManager.reload();
        this.gameManager.reload();

        // before ConfigManager
        this.placeholderManager.reload();
        this.configManager.reload();
        // after ConfigManager
        this.debugger = ConfigManager.debug() ? (s) -> logger.info("[DEBUG] " + s.toString()) : (s) -> {};

        this.coolDownManager.reload();
        this.translationManager.reload();
        this.marketManager.reload();
        this.statisticsManager.reload();
        this.bagManager.reload();
        this.storageManager.reload();
        this.fishingManager.reload();

        this.itemManager.load();
        this.eventManager.load();
        this.entityManager.load();
        this.lootManager.load();
        this.blockManager.load();
        this.effectManager.load();
        this.hookManager.load();
        this.totemManager.load();
        this.competitionManager.load();

        EventUtils.fireAndForget(new CustomFishingReloadEvent(this));
    }

    @Override
    public void disable() {
        this.eventManager.disable();
        this.configManager.disable();
        this.requirementManager.disable();
        this.actionManager.disable();
        this.placeholderManager.disable();
        this.itemManager.disable();
        this.competitionManager.disable();
        this.marketManager.disable();
        this.lootManager.disable();
        this.coolDownManager.disable();
        this.entityManager.disable();
        this.blockManager.disable();
        this.statisticsManager.disable();
        this.effectManager.disable();
        this.hookManager.disable();
        this.bagManager.disable();
        this.integrationManager.disable();
        this.storageManager.disable();
        this.commandManager.unregisterFeatures();
    }

    @Override
    public InputStream getResourceStream(String filePath) {
        return getBoostrap().getResource(filePath);
    }

    @Override
    public PluginLogger getPluginLogger() {
        return logger;
    }

    @Override
    public ClassPathAppender getClassPathAppender() {
        return classPathAppender;
    }

    @Override
    public Path getDataDirectory() {
        return getBoostrap().getDataFolder().toPath().toAbsolutePath();
    }

    @Override
    public String getServerVersion() {
        return Bukkit.getServer().getBukkitVersion().split("-")[0];
    }

    @SuppressWarnings("deprecation")
    @Override
    public String getPluginVersion() {
        return getBoostrap().getDescription().getVersion();
    }

    @Override
    public void debug(Object message) {
        this.debugger.accept(message);
    }
}
