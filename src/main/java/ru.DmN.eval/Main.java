package ru.DmN.eval;

import com.mojang.brigadier.arguments.StringArgumentType;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.text.LiteralText;

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
                context.getSource().sendFeedback(new LiteralText(gs.evaluate(context.getArgument("code", String.class)).toString()), false);
                return 1;
            })));
        });
    }
}
