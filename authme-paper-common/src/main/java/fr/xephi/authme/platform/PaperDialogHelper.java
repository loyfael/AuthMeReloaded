package fr.xephi.authme.platform;

import fr.xephi.authme.process.register.RegisterSecondaryArgument;
import fr.xephi.authme.process.register.RegistrationType;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Encapsulates the Paper-derived dialog API calls used by Paper and Folia.
 */
public final class PaperDialogHelper {

    private static volatile PaperDialogStyle style = PaperDialogStyle.DEFAULT;

    private PaperDialogHelper() {
    }

    /**
     * Sets the presentation style used for all dialogs built from now on.
     *
     * @param dialogStyle the style to apply
     */
    public static void useStyle(PaperDialogStyle dialogStyle) {
        style = Objects.requireNonNull(dialogStyle, "dialogStyle");
    }

    static void closeDialog(Player player) {
        player.closeDialog();
    }

    static void showLoginDialog(Player player, DialogWindowSpec dialog) {
        player.showDialog(createInGameCommandDialog(dialog, PaperDialogStyle.Kind.LOGIN, "login $(password)"));
    }

    static void showTotpDialog(Player player, DialogWindowSpec dialog) {
        player.showDialog(createInGameCommandDialog(dialog, PaperDialogStyle.Kind.TWO_FACTOR, "2fa code $(code)"));
    }

    static void showRegisterDialog(Player player, RegistrationType type, RegisterSecondaryArgument secondArg,
                                   DialogWindowSpec dialog) {
        player.showDialog(createInGameCommandDialog(dialog, PaperDialogStyle.Kind.REGISTER,
            createRegisterTemplate(type, secondArg)));
    }

    public static Dialog createPreJoinLoginDialog(DialogWindowSpec dialog) {
        DialogBase base = DialogBase.builder(createTitle(dialog))
            .inputs(createInputs(dialog))
            .body(createBody(dialog, PaperDialogStyle.Kind.LOGIN))
            .canCloseWithEscape(dialog.canCloseWithEscape())
            .afterAction(DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE)
            .build();

        ActionButton loginButton = createButton(dialog.primaryButtonLabel(), preJoinButtonCount(dialog))
            .action(DialogAction.customClick(PaperDialogActionKeys.PRE_JOIN_LOGIN_SUBMIT, null))
            .build();

        // Recovery button if the spec includes a secondary command, otherwise a plain cancel button
        Key secondaryKey = dialog.secondaryButtonCommand() != null
            ? PaperDialogActionKeys.PRE_JOIN_LOGIN_RECOVERY
            : PaperDialogActionKeys.PRE_JOIN_LOGIN_CANCEL;

        return Dialog.create(factory -> factory.empty()
            .base(base)
            .type(createActions(createPreJoinButtons(dialog, loginButton, secondaryKey))));
    }

    public static Dialog createPreJoinRecoveryDialog(DialogWindowSpec dialog) {
        DialogBase base = DialogBase.builder(createTitle(dialog))
            .inputs(createInputs(dialog))
            .body(createBody(dialog, PaperDialogStyle.Kind.RECOVERY))
            .canCloseWithEscape(false)
            .afterAction(DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE)
            .build();

        ActionButton submitButton = createButton(dialog.primaryButtonLabel(), preJoinButtonCount(dialog))
            .action(DialogAction.customClick(PaperDialogActionKeys.PRE_JOIN_RECOVERY_SUBMIT, null))
            .build();

        return Dialog.create(factory -> factory.empty()
            .base(base)
            .type(createActions(createPreJoinButtons(dialog, submitButton,
                PaperDialogActionKeys.PRE_JOIN_RECOVERY_CANCEL))));
    }

    public static Dialog createPreJoinRegisterDialog(DialogWindowSpec dialog) {
        DialogBase base = DialogBase.builder(createTitle(dialog))
            .inputs(createInputs(dialog))
            .body(createBody(dialog, PaperDialogStyle.Kind.REGISTER))
            .canCloseWithEscape(dialog.canCloseWithEscape())
            .afterAction(DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE)
            .build();

        ActionButton registerButton = createButton(dialog.primaryButtonLabel(), preJoinButtonCount(dialog))
            .action(DialogAction.customClick(PaperDialogActionKeys.PRE_JOIN_REGISTER_SUBMIT, null))
            .build();

        return Dialog.create(factory -> factory.empty()
            .base(base)
            .type(createActions(createPreJoinButtons(dialog,
                registerButton,
                PaperDialogActionKeys.PRE_JOIN_REGISTER_CANCEL))));
    }

    private static Dialog createInGameCommandDialog(DialogWindowSpec dialog, PaperDialogStyle.Kind kind,
                                                    String commandTemplate) {
        DialogBase base = DialogBase.builder(createTitle(dialog))
            .inputs(createInputs(dialog))
            .body(createBody(dialog, kind))
            .afterAction(DialogBase.DialogAfterAction.CLOSE)
            .build();

        boolean showSecondaryButton = dialog.showSecondaryButton() && dialog.secondaryButtonCommand() != null;
        int buttonCount = showSecondaryButton ? 2 : 1;

        ActionButton primaryButton = createButton(dialog.primaryButtonLabel(), buttonCount)
            .action(DialogAction.commandTemplate(commandTemplate))
            .build();

        List<ActionButton> buttons = new ArrayList<>();
        buttons.add(primaryButton);
        if (showSecondaryButton) {
            buttons.add(createButton(dialog.secondaryButtonLabel(), buttonCount)
                .action(DialogAction.commandTemplate(dialog.secondaryButtonCommand()))
                .build());
        }

        return Dialog.create(factory -> factory.empty()
            .base(base)
            .type(createActions(buttons)));
    }

    private static List<ActionButton> createPreJoinButtons(DialogWindowSpec dialog,
                                                           ActionButton primaryButton,
                                                           net.kyori.adventure.key.Key cancelActionKey) {
        List<ActionButton> buttons = new ArrayList<>();
        buttons.add(primaryButton);
        if (dialog.showSecondaryButton()) {
            buttons.add(createButton(dialog.secondaryButtonLabel(), preJoinButtonCount(dialog))
                .action(DialogAction.customClick(cancelActionKey, null))
                .build());
        }
        return buttons;
    }

    private static int preJoinButtonCount(DialogWindowSpec dialog) {
        return dialog.showSecondaryButton() ? 2 : 1;
    }

    private static Component createTitle(DialogWindowSpec dialog) {
        return style.title(createText(dialog.title()));
    }

    private static ActionButton.Builder createButton(String label, int buttonCount) {
        return style.button(ActionButton.builder(createText(label)), buttonCount);
    }

    private static DialogType createActions(List<ActionButton> buttons) {
        return style.actions(DialogType.multiAction(buttons), buttons.size()).build();
    }

    private static List<DialogInput> createInputs(DialogWindowSpec dialog) {
        List<DialogInput> inputs = new ArrayList<>();
        for (DialogInputSpec input : dialog.inputs()) {
            inputs.add(style.input(DialogInput.text(input.id(), createText(input.label()))
                .maxLength(input.maxLength()))
                .build());
        }
        return inputs;
    }

    private static List<DialogBody> createBody(DialogWindowSpec dialog, PaperDialogStyle.Kind kind) {
        if (dialog.body() == null) {
            return List.of();
        }
        return style.body(kind, createText(dialog.body()));
    }

    private static String createRegisterTemplate(RegistrationType type, RegisterSecondaryArgument secondArg) {
        if (type == RegistrationType.EMAIL) {
            if (secondArg == RegisterSecondaryArgument.CONFIRMATION) {
                return "register $(email) $(confirm)";
            }
            return "register $(email)";
        }

        if (secondArg == RegisterSecondaryArgument.CONFIRMATION) {
            return "register $(password) $(confirm)";
        }
        if (secondArg == RegisterSecondaryArgument.EMAIL_MANDATORY
            || secondArg == RegisterSecondaryArgument.EMAIL_OPTIONAL) {
            return "register $(password) $(email)";
        }
        return "register $(password)";
    }

    private static Component createText(String text) {
        return style.text(text);
    }
}
