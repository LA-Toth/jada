// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

/**
 * Configuration model and YAML loading pipeline.
 *
 * <p>This package provides:
 * <ul>
 *   <li>the root config object and validation exception ({@link me.laszloattilatoth.jada.config.Config}),</li>
 *   <li>parsing and semantic validation of YAML config sections ({@link me.laszloattilatoth.jada.config.ConfigLoader}),</li>
 *   <li>immutable proxy runtime descriptors ({@link me.laszloattilatoth.jada.config.ProxyConfig}),</li>
 *   <li>a base class for proxy-specific option models ({@link me.laszloattilatoth.jada.config.ProxyOptions}),</li>
 *   <li>reflection-based map-to-object binding and field validation annotations
 *       ({@link me.laszloattilatoth.jada.config.ReflectionConfigLoader}).</li>
 * </ul>
 *
 * <p>At startup, {@code Config.create(...)} reads YAML and delegates to {@code ConfigLoader}, which
 * converts each proxy entry into a {@code ProxyConfig} and resolves proxy-specific options via the
 * registration metadata provided by {@code me.laszloattilatoth.jada.proxy.core.registration}.
 */
package me.laszloattilatoth.jada.config;
