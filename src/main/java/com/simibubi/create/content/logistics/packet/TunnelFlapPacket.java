package com.simibubi.create.content.logistics.packet;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelTileEntity;
import com.simibubi.create.foundation.networking.TileEntityDataPacket;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;

public class TunnelFlapPacket extends TileEntityDataPacket<BeltTunnelTileEntity> {

    private List<Pair<Direction, Boolean>> flaps;

    protected TunnelFlapPacket() {}

    public void read(PacketBuffer buffer) {
        super.read(buffer);

        byte size = buffer.readByte();

        this.flaps = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            Direction direction = Direction.byIndex(buffer.readByte());
            boolean inwards = buffer.readBoolean();

            flaps.add(Pair.of(direction, inwards));
        }
    }

    public TunnelFlapPacket(BeltTunnelTileEntity tile, List<Pair<Direction, Boolean>> flaps) {
        super(tile.getPos());

        this.flaps = new ArrayList<>(flaps);
    }

    @Override
    protected void writeData(PacketBuffer buffer) {
        buffer.writeByte(flaps.size());

        for (Pair<Direction, Boolean> flap : flaps) {
            buffer.writeByte(flap.getLeft().getIndex());
            buffer.writeBoolean(flap.getRight());
        }
    }

    @Override
    protected void handlePacket(BeltTunnelTileEntity tile) {
        for (Pair<Direction, Boolean> flap : flaps) {
            tile.flap(flap.getLeft(), flap.getRight());
        }
    }
}
