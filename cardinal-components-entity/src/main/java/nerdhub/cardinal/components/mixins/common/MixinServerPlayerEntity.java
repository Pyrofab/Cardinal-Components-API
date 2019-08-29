/*
 * Cardinal-Components-API
 * Copyright (C) 2019 GlassPane
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package nerdhub.cardinal.components.mixins.common;

import nerdhub.cardinal.components.api.event.PlayerCopyCallback;
import nerdhub.cardinal.components.api.event.PlayerSyncCallback;
import nerdhub.cardinal.components.api.event.TrackingStartCallback;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity {
    @Inject(method = "onStartedTracking", at = @At("HEAD"))
    private void onStartedTracking(Entity tracked, CallbackInfo info) {
        TrackingStartCallback.EVENT.invoker().onPlayerStartTracking((ServerPlayerEntity)(Object)this, tracked);
    }

    @Inject(method = "changeDimension", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getStatusEffects()Ljava/util/Collection;"))
    private void onDimensionChange(DimensionType dimension, CallbackInfoReturnable<Entity> cir) {
        PlayerSyncCallback.EVENT.invoker().onPlayerSync((ServerPlayerEntity)(Object) this);
    }

    @Inject(method = "copyFrom", at = @At("RETURN"))
    private void copyDataFrom(ServerPlayerEntity original, boolean lossless, CallbackInfo ci) {
        PlayerCopyCallback.EVENT.invoker().copyData(original, (ServerPlayerEntity) (Object) this, lossless);
    }
}
