package com.tpwithgui.screen;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TeleportScreen extends Screen {
	private final List<PlayerButton> playerButtons = new ArrayList<>();
	private int scrollOffset = 0;
	private int maxScroll = 0;
	private String selectedPlayer = null;

	private static final int SPLIT_WIDTH = 200;
	private static final int LIST_TOP = 40;
	private static final int LIST_BOTTOM_OFFSET = 50;
	private static final int BUTTON_HEIGHT = 20;
	private static final int BUTTON_SPACING = 5;
	private static final int TOTAL_WIDTH = 400;
	private static final int TOTAL_HEIGHT = 250;

	private Button teleportToButton;
	private Button teleportToMeButton;
	private Button closeButton;

	public TeleportScreen() {
		super(Component.translatable("gui.tpwithgui.teleport_menu"));
	}

	@Override
	protected void init() {
		super.init();

		Minecraft client = Minecraft.getInstance();
		if (client.level == null || client.player == null) {
			return;
		}

		playerButtons.clear();
		scrollOffset = 0;
		selectedPlayer = null;

		for (var player : client.level.players()) {
			if (!player.getUUID().equals(client.player.getUUID())) {
				String name = player.getName().getString();
				Button button = Button.builder(Component.literal(name), btn -> selectPlayer(name))
						.bounds(20, 0, SPLIT_WIDTH - 40, BUTTON_HEIGHT)
						.build();
				this.addRenderableWidget(button);
				playerButtons.add(new PlayerButton(button, name));
			}
		}

		int listHeight = TOTAL_HEIGHT - LIST_TOP - LIST_BOTTOM_OFFSET;
		int totalContentHeight = playerButtons.size() * (BUTTON_HEIGHT + BUTTON_SPACING);
		maxScroll = Math.max(0, totalContentHeight - listHeight);

		int menuX = SPLIT_WIDTH + 20;
		int menuY = TOTAL_HEIGHT / 2 - 40;

		teleportToButton = Button.builder(Component.translatable("gui.tpwithgui.teleport_to_selected"), btn -> {
			Minecraft mc = Minecraft.getInstance();
			if (selectedPlayer != null && mc.getConnection() != null) {
				mc.getConnection().sendCommand("tp " + selectedPlayer);
				this.onClose();
			}
		}).bounds(menuX, menuY, TOTAL_WIDTH - SPLIT_WIDTH - 40, 20).build();

		teleportToMeButton = Button.builder(Component.translatable("gui.tpwithgui.teleport_selected_to_me"), btn -> {
			Minecraft mc = Minecraft.getInstance();
			if (selectedPlayer != null && mc.getConnection() != null && mc.player != null) {
				mc.getConnection().sendCommand("tp " + selectedPlayer + " " + mc.player.getName().getString());
				this.onClose();
			}
		}).bounds(menuX, menuY + 30, TOTAL_WIDTH - SPLIT_WIDTH - 40, 20).build();

		closeButton = Button.builder(Component.translatable("gui.tpwithgui.close"), btn -> this.onClose())
				.bounds(menuX, menuY + 70, TOTAL_WIDTH - SPLIT_WIDTH - 40, 20)
				.build();

		this.addRenderableWidget(teleportToButton);
		this.addRenderableWidget(teleportToMeButton);
		this.addRenderableWidget(closeButton);

		updateMenuButtonsState();
	}

	private void selectPlayer(String playerName) {
		selectedPlayer = playerName;
		updateMenuButtonsState();
	}

	private void updateMenuButtonsState() {
		boolean hasSelection = selectedPlayer != null;
		teleportToButton.active = hasSelection;
		teleportToMeButton.active = hasSelection;

		if (hasSelection) {
			teleportToButton.setMessage(Component.translatable("gui.tpwithgui.teleport_to", selectedPlayer));
			teleportToMeButton.setMessage(Component.translatable("gui.tpwithgui.teleport_to_me", selectedPlayer));
		} else {
			teleportToButton.setMessage(Component.translatable("gui.tpwithgui.teleport_to_selected"));
			teleportToMeButton.setMessage(Component.translatable("gui.tpwithgui.teleport_selected_to_me"));
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		int startX = (this.width - TOTAL_WIDTH) / 2;
		int startY = (this.height - TOTAL_HEIGHT) / 2;
		int listHeight = TOTAL_HEIGHT - LIST_TOP - LIST_BOTTOM_OFFSET;
		int y = startY + LIST_TOP - scrollOffset;

		for (PlayerButton playerButton : playerButtons) {
			Button button = playerButton.button;
			button.setX(startX + 20);
			button.setY(y);

			boolean isVisible = y + BUTTON_HEIGHT > startY + LIST_TOP && y < startY + LIST_TOP + listHeight;
			button.visible = isVisible;
			button.active = !playerButton.name.equals(selectedPlayer) && isVisible;

			y += BUTTON_HEIGHT + BUTTON_SPACING;
		}

		int menuX = startX + SPLIT_WIDTH + 20;
		int menuY = startY + TOTAL_HEIGHT / 2 - 40;

		teleportToButton.setX(menuX);
		teleportToButton.setY(menuY);
		teleportToMeButton.setX(menuX);
		teleportToMeButton.setY(menuY + 30);
		closeButton.setX(menuX);
		closeButton.setY(menuY + 70);

		super.extractRenderState(graphics, mouseX, mouseY, delta);

		graphics.centeredText(this.font, Component.translatable("gui.tpwithgui.online_players"),
				startX + SPLIT_WIDTH / 2, startY + 15, 0xFFFFFFFF);
		graphics.centeredText(this.font, Component.translatable("gui.tpwithgui.teleport_menu"),
				startX + SPLIT_WIDTH + (TOTAL_WIDTH - SPLIT_WIDTH) / 2, startY + 15, 0xFFFFFFFF);

		int selectedTextY = startY + TOTAL_HEIGHT / 2 - 80;
		if (selectedPlayer != null) {
			graphics.text(this.font, Component.translatable("gui.tpwithgui.selected_player", selectedPlayer),
					menuX, selectedTextY, 0xFFFFFFFF, true);
		} else {
			graphics.text(this.font, Component.translatable("gui.tpwithgui.no_player_selected"),
					menuX, selectedTextY, 0xFF808080, true);
		}

		if (maxScroll > 0) {
			renderScrollbar(graphics, startX, startY + LIST_TOP, listHeight);
		}
	}

	private void renderScrollbar(GuiGraphicsExtractor graphics, int startX, int startY, int listHeight) {
		int scrollbarX = startX + SPLIT_WIDTH - 15;
		int scrollbarWidth = 6;

		graphics.fill(scrollbarX, startY, scrollbarX + scrollbarWidth, startY + listHeight, 0xFF000000);

		int totalContentHeight = playerButtons.size() * (BUTTON_HEIGHT + BUTTON_SPACING);
		if (totalContentHeight <= 0 || maxScroll <= 0) {
			return;
		}
		float scrollRatio = (float) scrollOffset / maxScroll;
		int handleHeight = Math.max(20, (int) ((float) listHeight * listHeight / totalContentHeight));
		int handleY = startY + (int) (scrollRatio * (listHeight - handleHeight));

		graphics.fill(scrollbarX, handleY, scrollbarX + scrollbarWidth, handleY + handleHeight, 0xFF808080);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (maxScroll > 0) {
			int scrollAmount = (int) (verticalAmount * 20);
			scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - scrollAmount));
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void onClose() {
		this.minecraft.gui.setScreen(null);
	}

	private record PlayerButton(Button button, String name) {
	}
}
