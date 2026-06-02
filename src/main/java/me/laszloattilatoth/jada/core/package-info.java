// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

/**
 * Application orchestration for starting and wiring proxy runtimes.
 *
 * <p>This package contains the top-level runtime entry pieces that:
 * <ul>
 *   <li>initialize security and load configuration ({@link me.laszloattilatoth.jada.core.AppMain}),</li>
 *   <li>instantiate proxy implementations using the registration system ({@link me.laszloattilatoth.jada.core.ProxyFactory}),</li>
 *   <li>drive the NIO accept loop and dispatch accepted connections to the matching proxy runtime.</li>
 * </ul>
 *
 * <p>The package is intentionally small and focused on lifecycle concerns. Protocol-specific behavior
 * lives in {@code me.laszloattilatoth.jada.proxy.*}, while parsed settings are represented in
 * {@code me.laszloattilatoth.jada.config}.
 */
package me.laszloattilatoth.jada.core;
