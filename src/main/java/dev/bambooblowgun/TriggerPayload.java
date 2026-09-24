package dev.bambooblowgun;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
public record TriggerPayload(boolean held) implements CustomPacketPayload {
    public static final Type<TriggerPayload> TYPE = new Type<>(BlowgunMod.id("trigger"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TriggerPayload> CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, TriggerPayload::held, TriggerPayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}