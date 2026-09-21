package me.duncanruns.teamonlylocatorbar.mixin;

import me.duncanruns.teamonlylocatorbar.TeamOnlyLocatorBar;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.waypoints.WaypointTransmitter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.pac.common.server.api.OpenPACServerAPI;
import xaero.pac.common.server.parties.party.api.IServerPartyAPI;

import java.util.UUID;

@Mixin(WaypointTransmitter.class)
public interface WaypointTransmitterMixin {
    @Inject(method = "doesSourceIgnoreReceiver", at = @At("RETURN"), cancellable = true)
    private static void preventNonPartyTracking(LivingEntity source, ServerPlayer receiver, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) return;
        if (TeamOnlyLocatorBar.SPECTATORS_IGNORE_TEAMS && receiver.isSpectator()) return;
        if (!(source instanceof ServerPlayer sourcePlayer)) return;
        if (sourcePlayer == receiver) return; // always see yourself

        UUID sourceId = sourcePlayer.getUUID();
        UUID receiverId = receiver.getUUID();

        OpenPACServerAPI api = OpenPACServerAPI.get(receiver.getServer());
        IServerPartyAPI sourceParty = api.getPartyManager().getPartyByMember(sourceId);
        IServerPartyAPI receiverParty = api.getPartyManager().getPartyByMember(receiverId);

        if (sourceParty == null || receiverParty == null) {
            cir.setReturnValue(true); // no party = don't show
            return;
        }
        cir.setReturnValue(sourceParty.getId() != receiverParty.getId()
                && !sourceParty.getId().equals(receiverParty.getId()));
    }
}