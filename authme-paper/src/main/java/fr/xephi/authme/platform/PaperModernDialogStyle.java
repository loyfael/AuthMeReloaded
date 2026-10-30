package fr.xephi.authme.platform;

import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.MultiActionType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Dialog presentation for Paper: one aligned content column, bold titles, full-width descriptions
 * and fields, and buttons sized to fill the column.
 * Text, colors and actions still come from the dialog spec and messages file.
 */
final class PaperModernDialogStyle implements PaperDialogStyle {

    /** Width shared by descriptions, fields and button rows so every element lines up. */
    private static final int CONTENT_WIDTH = 250;
    /** Horizontal spacing the client leaves between buttons in the same row. */
    private static final int BUTTON_SPACING = 2;
    private static final int MAX_COLUMNS = 2;
    private static final Pattern MINI_MESSAGE_TAG = Pattern.compile("<[a-zA-Z#/!][^<>]*>");

    /** Messages containing MiniMessage tags are parsed as MiniMessage, all others keep legacy codes. */
    @Override
    public Component text(String text) {
        if (MINI_MESSAGE_TAG.matcher(text).find()) {
            return MiniMessage.miniMessage().deserialize(text);
        }
        return PaperDialogStyle.super.text(text);
    }

    @Override
    public Component title(Component title) {
        return title.decorationIfAbsent(TextDecoration.BOLD, TextDecoration.State.TRUE);
    }

    @Override
    public List<DialogBody> body(Kind kind, Component body) {
        // Item bodies are avoided: clients cannot render items during the pre-join configuration phase
        return List.of(DialogBody.plainMessage(body, CONTENT_WIDTH));
    }

    @Override
    public TextDialogInput.Builder input(TextDialogInput.Builder input) {
        return input.width(CONTENT_WIDTH);
    }

    @Override
    public ActionButton.Builder button(ActionButton.Builder button, int buttonCount) {
        int columns = columns(buttonCount);
        return button.width((CONTENT_WIDTH - BUTTON_SPACING * (columns - 1)) / columns);
    }

    @Override
    public MultiActionType.Builder actions(MultiActionType.Builder actions, int buttonCount) {
        return actions.columns(columns(buttonCount));
    }

    private static int columns(int buttonCount) {
        return Math.max(1, Math.min(buttonCount, MAX_COLUMNS));
    }
}
