package io.github.brassmc.brassloader.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class DirectionButton extends Button {
    private static final ResourceLocation DIRECTION_BUTTON = new ResourceLocation("brassloader", "textures/gui/direction_button.png");
    private final ModsList modsList;

    public DirectionButton(int x, int y, int width, int height, OnPress onPress, ModsList modsList) {
        super(x, y, width, height, ModsList.FilterDirection.NONE.asLiteral(), onPress);
        this.modsList = modsList;
    }

    @Override
    public void render(@NotNull PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        if(this.visible) {
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            Minecraft mc = Minecraft.getInstance();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            int yPos = getYImage(isHoveredOrFocused());
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            blit(stack, this.x, this.y, 0, 46 + yPos * 20, this.width / 2, this.height);
            blit(stack, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + yPos * 20, this.width / 2, this.height);
            if(this.modsList.getFilterDirection() != ModsList.FilterDirection.NONE) {
                RenderSystem.setShaderTexture(0, DIRECTION_BUTTON);
                blit(
                        stack,
                        this.x + this.width / 2 - 8,
                        this.y + this.height / 2 - 8,
                        this.modsList.getFilterDirection() == ModsList.FilterDirection.ASCENDING ? 0 : 16,
                        isHoveredOrFocused() ? 16 : 0,
                        16,
                        16,
                        32,
                        32
                );
            } else {
                int color = this.active ? 16777215 : 10526880;
                drawCenteredString(
                        stack,
                        mc.font,
                        ModsList.FilterDirection.NONE.asLiteral(),
                        this.x + this.width / 2,
                        this.y + (this.height - 8) / 2,
                        color | Mth.ceil(this.alpha * 255.0F) << 24
                );
            }
        }
    }
}
