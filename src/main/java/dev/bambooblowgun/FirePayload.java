package dev.bambooblowgun;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
public record FirePayload() implements CustomPacketPayload {
    public static final FirePayload INSTANCE = new FirePayload();
    public static final Type<FirePayload> TYPE = new Type<>(BlowgunMod.id("fire"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FirePayload> CODEC = StreamCodec.unit(INSTANCE);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}