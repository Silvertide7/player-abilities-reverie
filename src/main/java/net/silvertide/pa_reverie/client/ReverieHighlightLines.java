package net.silvertide.pa_reverie.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

public final class ReverieHighlightLines {

    private ReverieHighlightLines() {}

    public static void drawBox(VertexConsumer consumer, Matrix4f matrix,
                               float x0, float y0, float z0,
                               float x1, float y1, float z1,
                               int r, int g, int b, int a) {
        edgeAlongX(consumer, matrix, x0, x1, y0, z0, r, g, b, a);
        edgeAlongX(consumer, matrix, x0, x1, y0, z1, r, g, b, a);
        edgeAlongX(consumer, matrix, x0, x1, y1, z0, r, g, b, a);
        edgeAlongX(consumer, matrix, x0, x1, y1, z1, r, g, b, a);
        edgeAlongY(consumer, matrix, x0, y0, y1, z0, r, g, b, a);
        edgeAlongY(consumer, matrix, x0, y0, y1, z1, r, g, b, a);
        edgeAlongY(consumer, matrix, x1, y0, y1, z0, r, g, b, a);
        edgeAlongY(consumer, matrix, x1, y0, y1, z1, r, g, b, a);
        edgeAlongZ(consumer, matrix, x0, y0, z0, z1, r, g, b, a);
        edgeAlongZ(consumer, matrix, x0, y1, z0, z1, r, g, b, a);
        edgeAlongZ(consumer, matrix, x1, y0, z0, z1, r, g, b, a);
        edgeAlongZ(consumer, matrix, x1, y1, z0, z1, r, g, b, a);
    }

    private static void edgeAlongX(VertexConsumer consumer, Matrix4f matrix,
                                   float x0, float x1, float y, float z,
                                   int r, int g, int b, int a) {
        consumer.addVertex(matrix, x0, y, z).setColor(r, g, b, a).setNormal(1f, 0f, 0f);
        consumer.addVertex(matrix, x1, y, z).setColor(r, g, b, a).setNormal(1f, 0f, 0f);
    }

    private static void edgeAlongY(VertexConsumer consumer, Matrix4f matrix,
                                   float x, float y0, float y1, float z,
                                   int r, int g, int b, int a) {
        consumer.addVertex(matrix, x, y0, z).setColor(r, g, b, a).setNormal(0f, 1f, 0f);
        consumer.addVertex(matrix, x, y1, z).setColor(r, g, b, a).setNormal(0f, 1f, 0f);
    }

    private static void edgeAlongZ(VertexConsumer consumer, Matrix4f matrix,
                                   float x, float y, float z0, float z1,
                                   int r, int g, int b, int a) {
        consumer.addVertex(matrix, x, y, z0).setColor(r, g, b, a).setNormal(0f, 0f, 1f);
        consumer.addVertex(matrix, x, y, z1).setColor(r, g, b, a).setNormal(0f, 0f, 1f);
    }
}
