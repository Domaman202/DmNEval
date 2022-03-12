package ru.DmN.eval;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import java.util.Objects;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Main implements ModInitializer {
    public static final GroovyClassLoader gcl = new GroovyClassLoader();
    public static final Binding gb = new Binding();
    public static final GroovyShell gs = new GroovyShell(gcl, gb);

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(literal("eval").then(argument("code", StringArgumentType.greedyString()).executes(context -> {
                eval(context, context.getArgument("code", String.class));
                return 1;
            })));
            dispatcher.register(literal("evalb").executes(context -> {
                try {
                    var sb = new StringBuilder();
                    var nbt = (NbtList) context.getSource().getPlayer().getMainHandStack().getNbt().get("pages");
                    for (var e : nbt)
                        sb.append(e.asString().replace("\n", "")).append('\n');
                    sb.deleteCharAt(sb.length() - 1);
                    eval(context, sb.toString());
                } catch (Exception e) {
                    context.getSource().sendFeedback(new LiteralText("Error!"), false);
                }
                return 1;
            }));
        });
    }

    public static void eval(CommandContext<ServerCommandSource> context, String code) {
        try {
            gb.setProperty("context", context);
            gb.setProperty("cprint_", (IPrint) obj -> context.getSource().sendFeedback(new LiteralText(Objects.toString(obj)), false)); // void cprint(input) { ((ru.DmN.eval.Main.IPrint) cprint_).print(input) }
            gb.setProperty("cprint", gs.evaluate("(input) -> ((ru.DmN.eval.Main.IPrint) cprint_).print(input)"));
            context.getSource().sendFeedback(new LiteralText(Objects.toString(gs.evaluate(code))), false);
        } catch (Throwable t) {
            context.getSource().sendFeedback(new LiteralText(t.toString()), false);
            t.printStackTrace();
        }
    }

    @FunctionalInterface
    public interface IPrint {
        void print(Object obj);
    }
}

//context.getSource().getPlayer().getMainHandStack().getNbt().putString("");
//context.getSource().method_9207().method_6047().method_7969().method_10582("author", "Subject485")
