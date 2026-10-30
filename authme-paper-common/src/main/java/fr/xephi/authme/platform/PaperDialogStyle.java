package fr.xephi.authme.platform;

import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.MultiActionType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.List;

/**
 * Presentation hooks applied by {@link PaperDialogHelper} while building dialogs.
 * Styles only affect layout and text decoration: inputs, actions and lifecycle are left to the helper.
 * The default implementation renders dialogs exactly as the helper always has.
 */
public interface PaperDialogStyle {

    /** Unstyled rendering, shared by every Paper-derived platform unless it installs its own style. */
    PaperDialogStyle DEFAULT = new PaperDialogStyle() {
    };

    /** The kind of dialog being rendered, so styles can keep each window recognisable. */
    enum Kind {
        LOGIN,
        TWO_FACTOR,
        REGISTER,
        RECOVERY
    }

    /** Converts a resolved message (legacy section color codes) into a component. */
    default Component text(String text) {
        return LegacyComponentSerializer.legacySection().deserialize(text);
    }

    default Component title(Component title) {
        return title;
    }

    default List<DialogBody> body(Kind kind, Component body) {
        return List.of(DialogBody.plainMessage(body));
    }

    default TextDialogInput.Builder input(TextDialogInput.Builder input) {
        return input;
    }

    default ActionButton.Builder button(ActionButton.Builder button, int buttonCount) {
        return button;
    }

    default MultiActionType.Builder actions(MultiActionType.Builder actions, int buttonCount) {
        return actions;
    }
}
