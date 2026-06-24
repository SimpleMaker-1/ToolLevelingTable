package com.simplemaker.toolleveler.config;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simplemaker.toolleveler.ToolLeveler;
import com.simplemaker.toolleveler.config.util.CodecHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ItemValueConfig(
        long defaultItemWorth,
        Map<Either<Item, TagKey<Item>>, Long> values,
        Map<Item, Long> itemValues
) {
    private static final Codec<Item> CORRECT_ITEM =
            BuiltInRegistries.ITEM.byNameCodec();

    private static final Codec<Map<Either<Item, TagKey<Item>>, Long>> ITEM_TO_LONG =
            Codec.unboundedMap(
                    Codec.either(
                            CORRECT_ITEM,
                            TagKey.hashedCodec(Registries.ITEM)
                    ).flatXmap(ItemValueConfig::validate, DataResult::success),
                    CodecHelper.NON_NEGATIVE_LONG
            );

    public static final Codec<ItemValueConfig> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    CodecHelper.NON_NEGATIVE_LONG.fieldOf("default_item_worth")
                            .forGetter(ItemValueConfig::defaultItemWorth),
                    ITEM_TO_LONG.fieldOf("item_values")
                            .forGetter(ItemValueConfig::values)
            ).apply(instance, ItemValueConfig::new)
    );

    public ItemValueConfig(long defaultItemWorth, Map<Either<Item, TagKey<Item>>, Long> values) {
        this(defaultItemWorth, values, resolve(values));
    }

    private static final ItemValueConfig DEFAULT =
            new ItemValueConfig(5L, getDefaultItemValues());

    private static ItemValueConfig INSTANCE = DEFAULT;

    public static ItemValueConfig get()                { return INSTANCE; }
    public static void set(ItemValueConfig config)     { INSTANCE = config; }
    public static void setToDefault()                  { INSTANCE = DEFAULT; }

    private static DataResult<Either<Item, TagKey<Item>>> validate(Either<Item, TagKey<Item>> item) {
        if (item.left().isPresent()) {
            return validateItem(item.left().get()).map(Either::left);
        }
        return DataResult.success(item);
    }

    private static DataResult<Item> validateItem(Item item) {
        String id = item == null
                ? "unknown"
                : BuiltInRegistries.ITEM.getKey(item).toString();

        if (item == null || item == Items.AIR) {
            return DataResult.error(() -> "Item [" + id + "] not found in registry");
        }

        ItemStack stack = new ItemStack(item);

        if (stack.isDamageableItem()) {
            return DataResult.error(() -> "Item [" + id + "] is damageable — invalid for payment slot");
        }
        if (item.isEnchantable(stack)) {
            return DataResult.error(() -> "Item [" + id + "] is enchantable — invalid for payment slot");
        }

        return DataResult.success(item);
    }

    public static Map<Either<Item, TagKey<Item>>, Long> getDefaultItemValues() {
        Map<Either<Item, TagKey<Item>>, Long> v = new HashMap<>();

        // Coal
        v.put(Either.right(ItemTags.COALS),        8L);
        v.put(Either.right(ItemTags.COAL_ORES),   30L);
        v.put(Either.left(Items.COAL_BLOCK),       72L);

        // Copper
        v.put(Either.left(Items.RAW_COPPER),       11L);
        v.put(Either.left(Items.RAW_COPPER_BLOCK), 99L);
        v.put(Either.right(ItemTags.COPPER_ORES),  10L);
        v.put(Either.left(Items.COPPER_INGOT),     14L);
        v.put(Either.left(Items.COPPER_BLOCK),    126L);

        // Iron
        v.put(Either.left(Items.RAW_IRON),        13L);
        v.put(Either.left(Items.RAW_IRON_BLOCK),  117L);
        v.put(Either.right(ItemTags.IRON_ORES),   12L);
        v.put(Either.left(Items.IRON_INGOT),      15L);
        v.put(Either.left(Items.IRON_BLOCK),     135L);

        // Gold
        v.put(Either.left(Items.RAW_GOLD),        35L);
        v.put(Either.left(Items.RAW_GOLD_BLOCK),  315L);
        v.put(Either.right(ItemTags.GOLD_ORES),   30L);
        v.put(Either.left(Items.GOLD_INGOT),      40L);
        v.put(Either.left(Items.GOLD_BLOCK),     360L);

        // Diamond
        v.put(Either.left(Items.DIAMOND),              160L);
        v.put(Either.right(ItemTags.DIAMOND_ORES),     160L);
        v.put(Either.left(Items.DIAMOND_BLOCK),       1440L);

        // Netherite
        v.put(Either.left(Items.NETHERITE_SCRAP),      50L);
        v.put(Either.left(Items.ANCIENT_DEBRIS),       50L);
        v.put(Either.left(Items.NETHERITE_INGOT),     200L);
        v.put(Either.left(Items.NETHERITE_BLOCK),    1800L);

        // Lapis
        v.put(Either.left(Items.LAPIS_LAZULI),         8L);
        v.put(Either.right(ItemTags.LAPIS_ORES),     120L);
        v.put(Either.left(Items.LAPIS_BLOCK),         72L);

        // Emerald
        v.put(Either.left(Items.EMERALD),             100L);
        v.put(Either.right(ItemTags.EMERALD_ORES),    800L);
        v.put(Either.left(Items.EMERALD_BLOCK),       900L);

        // Quartz
        v.put(Either.left(Items.QUARTZ),              10L);
        v.put(Either.left(Items.NETHER_QUARTZ_ORE),   40L);
        v.put(Either.left(Items.QUARTZ_BLOCK),        40L);

        // Redstone
        v.put(Either.left(Items.REDSTONE),             6L);
        v.put(Either.right(ItemTags.REDSTONE_ORES),   60L);
        v.put(Either.left(Items.REDSTONE_BLOCK),      54L);

        // Glowstone
        v.put(Either.left(Items.GLOWSTONE_DUST),       6L);
        v.put(Either.left(Items.GLOWSTONE),           24L);

        // Misc blocks/items
        v.put(Either.left(Items.AMETHYST_BLOCK),      11L);
        v.put(Either.left(Items.AMETHYST_SHARD),      17L);
        v.put(Either.left(Items.PRISMARINE_SHARD),    15L);
        v.put(Either.left(Items.PRISMARINE_CRYSTALS), 15L);
        v.put(Either.left(Items.NAUTILUS_SHELL),      30L);
        v.put(Either.left(Items.HEART_OF_THE_SEA),  1000L);
        v.put(Either.left(Items.SEA_LANTERN),        140L);
        v.put(Either.left(Items.SPONGE),             150L);
        v.put(Either.left(Items.WET_SPONGE),         140L);

        // Food
        v.put(Either.left(Items.GOLDEN_APPLE),        400L);
        v.put(Either.left(Items.GOLDEN_CARROT),       100L);
        v.put(Either.left(Items.GLISTERING_MELON_SLICE), 100L);
        v.put(Either.left(Items.ENCHANTED_GOLDEN_APPLE), 2500L);

        // Drops
        v.put(Either.left(Items.SLIME_BALL),          25L);
        v.put(Either.left(Items.SLIME_BLOCK),        225L);
        v.put(Either.left(Items.ENDER_PEARL),         20L);
        v.put(Either.left(Items.BLAZE_ROD),           30L);
        v.put(Either.left(Items.ENDER_EYE),           50L);
        v.put(Either.left(Items.BLAZE_POWDER),        15L);
        v.put(Either.left(Items.MAGMA_CREAM),         50L);
        v.put(Either.left(Items.GHAST_TEAR),         200L);
        v.put(Either.left(Items.NETHER_STAR),       2500L);
        v.put(Either.left(Items.SHULKER_SHELL),      200L);
        v.put(Either.left(Items.END_CRYSTAL),        300L);
        v.put(Either.left(Items.EXPERIENCE_BOTTLE),  100L);
        v.put(Either.left(Items.DRAGON_EGG),        2000L);
        v.put(Either.left(Items.DRAGON_HEAD),       2000L);

        // Decorative
        v.put(Either.left(Items.ENDER_CHEST), 140L);
        v.put(Either.left(Items.BEACON),     2500L);

        return v;
    }

    private static Map<Item, Long> resolve(Map<Either<Item, TagKey<Item>>, Long> values) {
        Map<Item, Long> resolved = new HashMap<>();

        // Tags first (lower priority)
        values.entrySet().stream()
                .filter(e -> e.getKey().right().isPresent())
                .forEach(e -> {
                    TagKey<Item> tag = e.getKey().right().get();
                    ToolLeveler.LOGGER.info("Resolving items for tag: {}", tag.location());
                    for (Item item : getAllFromTag(tag)) {
                        if (resolved.containsKey(item)) {
                            ToolLeveler.LOGGER.warn("Duplicate item value for {} in tag {}, overriding",
                                    BuiltInRegistries.ITEM.getKey(item), tag.location());
                        }
                        resolved.put(item, e.getValue());
                    }
                });

        // Explicit items override tags
        values.entrySet().stream()
                .filter(e -> e.getKey().left().isPresent())
                .forEach(e -> {
                    Item item = e.getKey().left().get();
                    if (resolved.containsKey(item)) {
                        ToolLeveler.LOGGER.warn("Duplicate item value for {}, overriding",
                                BuiltInRegistries.ITEM.getKey(item));
                    }
                    resolved.put(item, e.getValue());
                });

        return resolved;
    }

    public static List<Item> getAllFromTag(TagKey<Item> tagKey) {
        List<Item> list = new ArrayList<>();
        BuiltInRegistries.ITEM.getTagOrEmpty(tagKey)
                .forEach(holder -> list.add(holder.value()));
        return list;
    }
}