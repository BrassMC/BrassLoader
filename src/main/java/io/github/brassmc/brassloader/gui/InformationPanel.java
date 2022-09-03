package io.github.brassmc.brassloader.gui;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import io.github.brassmc.brassloader.boot.discovery.ModDiscovery;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InformationPanel {

    static final Map<String, Renderer> PANELS = new HashMap<>() {{
        this.put("brass", createDefaultRenderer());
    }};

    public static void registerPanel(@Nonnull String modid, Renderer renderer) {
        if (modid == null || ModDiscovery.getMods().stream().noneMatch(c -> c.modid().equals(modid)))
            throw new IllegalArgumentException("Attempted to create information panel with null, empty or non-present modid.");
        if (renderer != null)
            PANELS.put(modid, renderer);
    }

    @FunctionalInterface
    public interface Renderer {

        void render(ModsListScreen screen, PoseStack poseStack, int mouseX, int mouseY, float partialTicks);
    }

    private static Renderer createDefaultRenderer() {
        return (screen, poseStack, mouseX, mouseY, partialTicks) -> {
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.getBuilder();

            ModsListScreen.renderScrollbar(tesselator, buffer, screen.getBoxX1() - 6, 200, 50, screen.getBoxX(), screen.getBoxY(), screen.getBoxX1(), screen.getBoxY1());

            poseStack.pushPose();
            poseStack.scale(1.5f, 1.5f, 1.5f);
            poseStack.translate((screen.getBoxX() + screen.getBoxWidth() / 2f) / 1.5f, screen.getBoxY() + screen.getBoxPadding(), 0f);
            GuiComponent.drawCenteredString(poseStack, screen.getFont(), screen.getSelectedMod().name(), 0, 0, 0xFFFFFF);
            poseStack.popPose();

            GuiComponent.drawCenteredString(poseStack, screen.getFont(), screen.getSelectedMod().modid(), screen.getBoxX() + screen.getBoxWidth() / 2, screen.getBoxY() + 35, 0xBABABA);
            GuiComponent.drawCenteredString(poseStack, screen.getFont(), screen.getSelectedMod().version(), screen.getBoxX() + screen.getBoxWidth() / 2, screen.getBoxY() + 45, 0x929292);

            List<FormattedCharSequence> description = Language.getInstance().getVisualOrder(screen.getFont().getSplitter().splitLines(screen.getSelectedMod().description(), screen.getBoxWidth() - 20, Style.EMPTY));
            int descriptionHeight = description.size() * screen.getFont().lineHeight;
            for (FormattedCharSequence line : description) {
                int lineY = screen.getBoxY() + screen.getBoxPadding() + 45 + 10 + (screen.getFont().lineHeight * description.indexOf(line));
                if (lineY > screen.getBoxY() && lineY < screen.getBoxY() + screen.getBoxHeight() - screen.getFont().lineHeight) {
                    GuiComponent.drawString(poseStack, screen.getFont(), line, screen.getBoxX() + screen.getBoxPadding(), lineY, 0xFFFFFF);
                }
            }

            MutableComponent license = Component.literal(screen.getSelectedMod().license());
            if (ModsListScreen.isMouseOver(mouseX, mouseY, screen.getBoxX() + screen.getBoxPadding(), screen.getBoxY() + screen.getBoxPadding() + 50 + descriptionHeight + screen.getBoxPadding(), screen.getFont().width(screen.getSelectedMod().license()), screen.getFont().lineHeight)) {
                license = license.withStyle(ChatFormatting.UNDERLINE);
            }

            GuiComponent.drawString(poseStack, screen.getFont(), license, screen.getBoxX() + screen.getBoxPadding(), screen.getBoxY() + screen.getBoxPadding() + 50 + descriptionHeight + screen.getBoxPadding(), 0xFFFFFF);

            GuiComponent.drawString(poseStack, screen.getFont(), Component.translatable("brassloader.screen.modsList.developers"), screen.getBoxX() + screen.getBoxPadding(), screen.getBoxY() + screen.getBoxPadding() + 50 + descriptionHeight + screen.getBoxPadding(), 0xFFFFFF);
        };
    }
}