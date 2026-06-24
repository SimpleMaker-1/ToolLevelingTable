package com.simplemaker.toolleveler.platform;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import java.util.function.Supplier;

public class RegistryObject<T> implements Supplier<T> {
    private final DeferredHolder<?, ?> holder;

    public RegistryObject(DeferredHolder<?, ?> holder) {
        this.holder = holder;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get() {
        return (T) this.holder.get();
    }

    public ResourceLocation getId() {
        return this.holder.getId();
    }

    @SuppressWarnings("unchecked")
    public Holder<T> asHolder() {
        return (Holder<T>) this.holder;
    }

    public DeferredHolder<?, ?> getHolder() {
        return this.holder;
    }
}
