/*
 * Copyright 2026 Hushfeed contributors
 * https://github.com/SysAdminDoc/hushfeed
 */
package app.morphe.patches.tiktok.interaction.uiscale

import app.morphe.patcher.Fingerprint
import app.morphe.patches.shared.compat.AppCompatibilities
import app.morphe.patches.tiktok.misc.extension.sharedExtensionPatch
import app.morphe.patches.tiktok.misc.settings.SettingsStatusLoadFingerprint
import app.morphe.patches.tiktok.misc.settings.configurableBytecodePatch as bytecodePatch
import app.morphe.patches.tiktok.misc.settings.settingsPatch
import app.morphe.util.addInstruction
import app.morphe.util.addInstructions

private const val PATCH = "UI scale"
private const val EXTENSION = "Lapp/morphe/extension/tiktok/font/UiScale;"
private const val CONTEXT = "Landroid/content/Context;"

/**
 * AppCompatActivity's attachBaseContext, which nearly every TikTok screen inherits. The class
 * name is obfuscated (X.02cp on 47.7.1, X.02cz on 47.9.2); TikTok's build tooling leaves methods
 * named after it in the class.
 */
internal object UiScaleAttachBaseContextFingerprint : Fingerprint(
    name = "attachBaseContext",
    returnType = "V",
    parameters = listOf(CONTEXT),
    custom = { _, classDef ->
        classDef.methods.any { it.name.startsWith("androidx_appcompat_app_AppCompatActivity_") }
    },
)

@Suppress("unused")
val uiScalePatch = bytecodePatch(
    name = PATCH,
    description = "Makes TikTok's whole interface smaller, text and buttons alike, so more fits " +
        "on screen. Starts at 100 %, which changes nothing. Pick a size in Hushfeed settings > " +
        "App, then restart TikTok.",
) {
    category("Interface")
    dependsOn(settingsPatch, sharedExtensionPatch)
    compatibleWith(*AppCompatibilities.tiktok())

    execute {
        SettingsStatusLoadFingerprint.method.addInstruction(
            0,
            "invoke-static {}, Lapp/morphe/extension/tiktok/settings/SettingsStatus;->enableUiScale()V",
        )

        // TikTok's own text size manager rewrites the density with Resources.updateConfiguration,
        // which Android recomputes from the configuration's densityDpi, so a scaled factor there
        // changes nothing. Each screen's context gets a lower densityDpi instead.
        UiScaleAttachBaseContextFingerprint.method.addInstructions(
            0,
            """
                invoke-static/range {p1 .. p1}, $EXTENSION->wrap($CONTEXT)$CONTEXT
                move-result-object p1
            """,
        )
    }
}
