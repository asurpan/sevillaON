package com.sagon.on

import com.sagon.on.LuxeColors

/**
 * 🛠️ MÓDULO PROFESIONAL - TERMINAL DE OPERACIONES
 * DISEÑADO PARA SER AISLADO Y SEGURO.
 * CUMPLE CON REGLAS DE GOOGLE PLAY Y LEGALIDAD EUROPEA.
 */

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import org.jetbrains.compose.resources.painterResource
import on.shared.generated.resources.Res
import on.shared.generated.resources.logo

@Composable
fun ProfessionalTerminal(
    state: RadioState,
    users: List<RemoteUser>,
    onStateChange: (RadioState) -> Unit,
    onReportPro: (String) -> Unit,
    onNotification: (AppNotification) -> Unit,
    onSharePro: (String?) -> Unit,
    onReplayPro: () -> Unit,
    onMicPro: (Boolean, Float) -> Unit,
    myPower: Float,
    onGpsRequest: (callback: (String?) -> Unit) -> Unit,
    onHertzSentinelRequest: () -> Unit = {},
    onClose: () -> Unit
) {
    var terminalStep by remember { 
        mutableStateOf(if (!state.hasSeenProIntro) 0 else 3) 
    }
    var helpStep by remember { mutableStateOf(0) } 
    var showRadarMagicDialog by remember { mutableStateOf(false) }
    var showSocialShare by remember { mutableStateOf(false) }
    var showMicRestrictedDialog by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    if (showMicRestrictedDialog) {
        AlertDialog(
            onDismissRequest = { showMicRestrictedDialog = false },
            containerColor = LuxeColors.DeepSea,
            titleContentColor = LuxeColors.Gold,
            textContentColor = Color.White,
            modifier = Modifier.border(1.dp, LuxeColors.Gold.copy(0.3f), RoundedCornerShape(12.dp)),
            icon = { Icon(Icons.Rounded.Lock, null, tint = LuxeColors.Gold, modifier = Modifier.size(40.dp)) },
            title = { Text("FUNCIÓN RESTRINGIDA", fontWeight = FontWeight.Black) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Estás en modo ESPECTADOR. Para poder emitir mensajes y ofertas en el Radar Pro, primero debes configurar tu perfil profesional.",
                        fontSize = 13.sp,
                        color = Color.White.copy(0.7f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "¿Quieres elegir ahora tus servicios u oficios?",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LuxeColors.Gold,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                LuxeButton(
                    text = "CONFIGURAR MI PERFIL",
                    onClick = { 
                        showMicRestrictedDialog = false 
                        terminalStep = 1 
                    },
                    enabled = true,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    containerColor = LuxeColors.Gold,
                    contentColor = Color.Black
                )
            },
            dismissButton = {
                TextButton(onClick = { showMicRestrictedDialog = false }) {
                    Text("LUEGO", color = Color.White.copy(0.4f))
                }
            }
        )
    }

    if (showSocialShare) {
        SocialShareSheet(
            state = state,
            onDismiss = { showSocialShare = false },
            onShareAction = { platform ->
                onSharePro(platform)
                showSocialShare = false
            }
        )
    }

    if (showRadarMagicDialog) {
        AlertDialog(
            onDismissRequest = { showRadarMagicDialog = false },
            containerColor = LuxeColors.DeepSea,
            titleContentColor = LuxeColors.Gold,
            textContentColor = Color.White,
            modifier = Modifier.border(1.dp, LuxeColors.Gold.copy(0.3f), RoundedCornerShape(12.dp)),
            icon = { Icon(Icons.Rounded.AutoAwesome, null, tint = LuxeColors.Gold, modifier = Modifier.size(40.dp)) },
            title = { Text("CÓMO FUNCIONA EL RADAR PRO", fontWeight = FontWeight.Black) },
            text = {
                Column {
                    Text("El Radar PRO es un sistema de filtrado inteligente que conecta ofertas y demandas de forma automática:", fontSize = 13.sp, color = Color.White.copy(0.7f))
                    Spacer(Modifier.height(20.dp))
                    
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Rounded.FilterAlt, null, tint = LuxeColors.ElectricBlue, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            val myRole = getRoleById(state.myProRole).name
                            Text("TU FILTRO DIGITAL:", fontSize = 12.sp, fontWeight = FontWeight.Black, color = LuxeColors.ElectricBlue)
                            Text("Basado en tu perfil actual ($myRole), el sistema sabe qué mensajes te interesan y cuáles no.", fontSize = 11.sp, color = Color.White.copy(0.7f))
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Rounded.CellTower, null, tint = LuxeColors.Gold, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("TRANSMISIÓN DIRIGIDA:", fontSize = 12.sp, fontWeight = FontWeight.Black, color = LuxeColors.Gold)
                            Text("Cuando hablas por el micro del Radar PRO, el sistema envía una alerta inmediata SOLO a las personas que coinciden con tu búsqueda.", fontSize = 11.sp, color = Color.White.copy(0.7f))
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Rounded.NotificationsActive, null, tint = LuxeColors.Green, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("NOTIFICACIÓN Y REBOBINADO:", fontSize = 12.sp, fontWeight = FontWeight.Black, color = LuxeColors.Green)
                            Text("Los interesados reciben una notificación y pueden oír tu mensaje desde el principio, aunque ya hayas terminado de hablar.", fontSize = 11.sp, color = Color.White.copy(0.7f))
                        }
                    }
                }
            },
            confirmButton = {
                LuxeButton(
                    text = "¡ENTENDIDO!",
                    onClick = { 
                        showRadarMagicDialog = false 
                        onStateChange(state.copy(hasSeenRadarMagic = true))
                    },
                    enabled = true,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    containerColor = LuxeColors.Gold,
                    contentColor = Color.Black
                )
            }
        )
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxeColors.DeepSea)
            .clickable(enabled = false) { }
    ) {
        when (terminalStep) {
            0 -> ProfessionalIntro(
                onAccept = { terminalStep = 1 }
            )
            1 -> ProfessionalObjectiveSelection(
                onSelect = { selection ->
                    when(selection) {
                        "BUSCO" -> {
                            onStateChange(state.copy(isProSeeking = false, isJustBrowsing = false))
                            terminalStep = 2
                        }
                        "OFREZCO" -> {
                            onStateChange(state.copy(isProSeeking = true, isJustBrowsing = false))
                            terminalStep = 2
                        }
                        "CURIOSEANDO" -> {
                            onStateChange(state.copy(isProSeeking = false, isJustBrowsing = true, myProRole = "CIUDADANO"))
                            terminalStep = 3
                            if (!state.hasSeenProIntro) {
                                onStateChange(state.copy(hasSeenProIntro = true))
                            }
                        }
                        else -> {}
                    }
                }
            )
            2 -> ProfessionalRoleSelection(
                state = state,
                onSelect = { roleId ->
                    onStateChange(state.copy(myProRole = roleId))
                    terminalStep = 3
                    if (!state.hasSeenProIntro) {
                        onStateChange(state.copy(hasSeenProIntro = true))
                    }
                },
                onBack = { terminalStep = 1 }
            )
            3 -> {
                ProfessionalContent(
                    state = state,
                    users = users,
                    onStateChange = onStateChange,
                    onReportPro = onReportPro,
                    onNotification = onNotification,
                    onSharePro = onSharePro,
                    onReplayPro = onReplayPro,
                    onMicPro = onMicPro,
                    myPower = myPower,
                    onHertzSentinelRequest = onHertzSentinelRequest,
                    onClose = onClose,
                    onShowHelp = { helpStep = 1 },
                    onShowRadarMagic = { showRadarMagicDialog = true },
                    onShowSocialShare = { showSocialShare = true },
                    onChangeSetup = { terminalStep = 1 }
                )
                
                if (helpStep > 0) {
                    ProfessionalTutorial(
                        step = helpStep,
                        onNext = { helpStep++ },
                        onClose = { helpStep = 0 }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfessionalObjectiveSelection(onSelect: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(LuxeColors.DeepSea, Color.Black)))
    ) {
        StarryBackground(activity = 0.3f, isEcoMode = false)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "¿CUÁL ES TU OBJETIVO?",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Text(
                "PERSONALIZA TU EXPERIENCIA PROFESIONAL",
                color = LuxeColors.Gold,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(48.dp))

            // Opción: BUSCO SERVICIOS
            SelectionCard(
                title = "BUSCO SERVICIOS",
                subtitle = "NECESITO AYUDA",
                desc = "Quiero encontrar un profesional o solicitar un servicio en directo.",
                icon = Icons.Rounded.Search,
                color = LuxeColors.Gold,
                onClick = { onSelect("OFREZCO") }
            )

            Spacer(Modifier.height(16.dp))

            // Opción: OFREZCO SERVICIOS
            SelectionCard(
                title = "OFREZCO SERVICIOS",
                subtitle = "SOY PROFESIONAL",
                desc = "Quiero ofrecer mi oficio y recibir alertas de ciudadanos en directo.",
                icon = Icons.Rounded.Engineering,
                color = LuxeColors.ElectricBlue,
                onClick = { onSelect("BUSCO") }
            )

            Spacer(Modifier.height(16.dp))

            // Opción: CURIOSEANDO
            SelectionCard(
                title = "SÓLO CURIOSEANDO",
                subtitle = "MODO ESPECTADOR",
                desc = "Ver ofertas activas sin participar ni mostrar mi perfil.",
                icon = Icons.Rounded.Visibility,
                color = Color.White.copy(0.4f),
                onClick = { onSelect("CURIOSEANDO") }
            )

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SelectionCard(
    title: String,
    subtitle: String,
    desc: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Black.copy(0.4f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(color.copy(0.1f), CircleShape)
                    .border(1.dp, color.copy(0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(20.dp))
            Column {
                Text(subtitle, color = color, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Text(title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text(desc, color = Color.White.copy(0.5f), fontSize = 11.sp, lineHeight = 14.sp)
            }
        }
    }
}

@Composable
private fun ProfessionalRoleSelection(
    state: RadioState,
    onSelect: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = Color.White.copy(0.5f))
            }
        }

        Icon(
            if (state.isProSeeking) Icons.Rounded.Search else Icons.Rounded.Handshake, 
            null, 
            tint = if (state.isProSeeking) LuxeColors.Gold else LuxeColors.ElectricBlue, 
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(Modifier.height(24.dp))
        
        Text(
            if (state.isProSeeking) "¿A QUIÉN BUSCAS?" else "¿CUÁL ES TU OFICIO?",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(16.dp))
        
        Text(
            if (state.isProSeeking) 
                "Selecciona la categoría profesional que necesitas contratar. Tu oferta llegará directamente a sus terminales."
            else 
                "Esto activará el Radar Pro. Solo recibirás avisos cuando alguien busque un profesional de tu sector.",
            color = Color.White.copy(0.6f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
        
        Spacer(Modifier.height(40.dp))

        PROFESSIONAL_ROLES.filter { it.id != "CIUDADANO" }.forEach { role ->
            val isSelected = state.myProRole == role.id
            Surface(
                onClick = { onSelect(role.id) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                color = if (isSelected) LuxeColors.Green.copy(0.12f) else Color.White.copy(0.04f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (isSelected) LuxeColors.Green else Color.White.copy(0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(44.dp).background(if (isSelected) LuxeColors.Green.copy(0.2f) else Color.White.copy(0.08f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(role.icon, null, tint = if (isSelected) LuxeColors.Green else Color.White.copy(0.4f))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(role.name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                        Text(role.description, color = Color.White.copy(0.5f), fontSize = 10.sp)
                    }
                }
            }
        }
        
        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun ProfessionalIntro(onAccept: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(LuxeColors.DeepSea, Color.Black)))
    ) {
        // Efecto de fondo sutil
        StarryBackground(activity = 0.2f, isEcoMode = false)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo o Icono Principal con Glow
            Box(contentAlignment = Alignment.Center) {
                Box(
                    Modifier.size(100.dp).background(LuxeColors.ElectricBlue.copy(0.15f), CircleShape)
                )
                Icon(
                    Icons.Rounded.Handshake,
                    null,
                    tint = LuxeColors.ElectricBlue,
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            Text(
                "TERMINAL PRO",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp
            )
            Text(
                "RED DE SERVICIOS EN TIEMPO REAL",
                color = LuxeColors.ElectricBlue,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(40.dp))

            IntroItem(
                icon = Icons.Rounded.VerifiedUser,
                title = "SISTEMA DE REPUTACIÓN",
                desc = "Eleva tu estatus colaborando con respeto. La red premia la profesionalidad."
            )

            IntroItem(
                icon = Icons.Rounded.Radar,
                title = "RADAR INTELIGENTE",
                desc = "Recibe avisos directos cuando alguien necesite tu oficio en tu zona."
            )

            IntroItem(
                icon = Icons.Rounded.GppMaybe,
                title = "PROTOCOLO SOS",
                desc = "Acceso a alertas de emergencia con ubicación GPS compartida."
            )

            Spacer(Modifier.height(48.dp))

            LuxeButton(
                text = "ENTRAR AL PANEL",
                onClick = onAccept,
                enabled = true,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                containerColor = LuxeColors.ElectricBlue,
                contentColor = Color.White
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun IntroItem(icon: ImageVector, title: String, desc: String) {
    Surface(
        color = Color.White.copy(0.03f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
        border = BorderStroke(1.dp, Color.White.copy(0.08f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(LuxeColors.ElectricBlue.copy(0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = LuxeColors.ElectricBlue, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(20.dp))
            Column {
                Text(
                    title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    desc,
                    color = Color.White.copy(0.5f),
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ProfessionalContent(
    state: RadioState,
    users: List<RemoteUser>,
    onStateChange: (RadioState) -> Unit,
    onReportPro: (String) -> Unit,
    onNotification: (AppNotification) -> Unit,
    onSharePro: (String?) -> Unit,
    onReplayPro: () -> Unit,
    onMicPro: (Boolean, Float) -> Unit,
    myPower: Float,
    onHertzSentinelRequest: () -> Unit,
    onShowHelp: () -> Unit,
    onShowRadarMagic: () -> Unit,
    onShowSocialShare: () -> Unit,
    onChangeSetup: () -> Unit,
    onClose: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    var showOfferAlert by remember { mutableStateOf(false) }
    var lastEmployerNick by remember { mutableStateOf("") }
    var lastEmployerId by remember { mutableStateOf("") }
    var showMicRestrictedDialog by remember { mutableStateOf(false) }

    if (showMicRestrictedDialog) {
        AlertDialog(
            onDismissRequest = { showMicRestrictedDialog = false },
            containerColor = LuxeColors.DeepSea,
            titleContentColor = LuxeColors.Gold,
            textContentColor = Color.White,
            modifier = Modifier.border(1.dp, LuxeColors.Gold.copy(0.3f), RoundedCornerShape(12.dp)),
            icon = { Icon(Icons.Rounded.Lock, null, tint = LuxeColors.Gold, modifier = Modifier.size(40.dp)) },
            title = { Text("FUNCIÓN RESTRINGIDA", fontWeight = FontWeight.Black) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Estás en modo ESPECTADOR. Para poder emitir mensajes y ofertas en el Radar Pro, primero debes configurar tu perfil profesional.",
                        fontSize = 13.sp,
                        color = Color.White.copy(0.7f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "¿Quieres elegir ahora tus servicios u oficios?",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LuxeColors.Gold,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                LuxeButton(
                    text = "CONFIGURAR MI PERFIL",
                    onClick = { 
                        showMicRestrictedDialog = false 
                        onChangeSetup() 
                    },
                    enabled = true,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    containerColor = LuxeColors.Gold,
                    contentColor = Color.Black
                )
            },
            dismissButton = {
                TextButton(onClick = { showMicRestrictedDialog = false }) {
                    Text("LUEGO", color = Color.White.copy(0.4f))
                }
            }
        )
    }

    val currentEmployer = remember(users) {
        users.find { 
            it.city == state.city && 
            it.isProSeeking && 
            it.isTransmitting && 
            state.myProRole != "CIUDADANO" &&
            (it.proRole == state.myProRole || it.proRole == "GENERAL") 
        }
    }

    LaunchedEffect(currentEmployer) {
        if (currentEmployer != null) {
            showOfferAlert = true
            lastEmployerNick = currentEmployer.nick
            lastEmployerId = currentEmployer.id
            triggerUiSound("switch")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Rounded.Handshake, null, tint = LuxeColors.ElectricBlue, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("TERMINAL PRO", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                Text("SERVICIOS Y SEGURIDAD", color = LuxeColors.ElectricBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            IconButton(onClick = onShowHelp) {
                Icon(Icons.Rounded.Info, null, tint = LuxeColors.Gold.copy(0.7f))
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Rounded.Close, null, tint = Color.White.copy(0.3f))
            }
        }

        Spacer(Modifier.height(20.dp))

        // --- 🔘 ESTADO ACTUAL (RESUMEN) ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White.copy(0.04f),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color.White.copy(0.08f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(40.dp).background(LuxeColors.Green.copy(0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            getRoleById(state.myProRole).icon, 
                            null, 
                            tint = LuxeColors.Green,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        val roleName = if (state.isJustBrowsing) "MODO ESPECTADOR" else getRoleById(state.myProRole).name
                        val intentLabel = when {
                            state.isJustBrowsing -> "CURIOSEANDO RED"
                            state.isProSeeking -> "BUSCO: $roleName"
                            else -> "SOY: $roleName"
                        }
                        Text(intentLabel, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
                        Text("EN ${state.city}", color = Color.White.copy(0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                LuxeButton(
                    text = "CAMBIAR",
                    onClick = onChangeSetup,
                    enabled = true,
                    modifier = Modifier.height(36.dp).width(110.dp),
                    containerColor = Color.White.copy(0.1f),
                    contentColor = Color.White,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // --- 🟢 INTERRUPTOR DE ESTADO MAESTRO (DISPONIBILIDAD) ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clickable { 
                    if (state.isJustBrowsing || state.myProRole == "CIUDADANO") {
                        showMicRestrictedDialog = true
                    } else {
                        onStateChange(state.copy(myWorkStatus = !state.myWorkStatus)) 
                    }
                },
            color = if (state.myWorkStatus) LuxeColors.Green.copy(0.08f) else Color.White.copy(0.02f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(
                2.dp, 
                if (state.myWorkStatus) LuxeColors.Green.copy(0.6f) 
                else Color.White.copy(0.1f)
            )
        ) {
            Box(Modifier.fillMaxSize()) {
                // Brillo de fondo si está activo
                if (state.myWorkStatus) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Brush.radialGradient(
                                colors = listOf(LuxeColors.Green.copy(0.15f), Color.Transparent),
                                radius = 300f
                            ))
                    )
                }

                Row(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val infiniteTransition = rememberInfiniteTransition()
                    val glowAlpha by infiniteTransition.animateFloat(
                        0.4f, 1f, infiniteRepeatable(tween(1200), RepeatMode.Reverse)
                    )
                    
                    // Indicador Visual (LED)
                    Box(contentAlignment = Alignment.Center) {
                        if (state.myWorkStatus) {
                            Box(
                                Modifier
                                    .size(24.dp)
                                    .graphicsLayer(alpha = glowAlpha)
                                    .background(LuxeColors.Green.copy(0.3f), CircleShape)
                            )
                        }
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(if (state.myWorkStatus) LuxeColors.Green else Color.White.copy(0.2f))
                        )
                    }
                    
                    Spacer(Modifier.width(20.dp))
                    
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (state.myWorkStatus) "ESTADO: DISPONIBLE" else "MODO: ESCUCHA",
                            color = if (state.myWorkStatus) LuxeColors.Green else Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (state.myWorkStatus) 
                                "ACTIVADO: Se mostrará una ETIQUETA PROFESIONAL junto a tu nombre en todos los canales. La red sabrá que estás libre para trabajar."
                            else 
                                "OCULTO: Tu nombre aparecerá sin etiquetas. Puedes oír todo, pero nadie sabrá que ofreces servicios en este momento.",
                            color = Color.White.copy(0.6f),
                            fontSize = 10.sp,
                            lineHeight = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Icono de Candado/Ojo según estado
                    Icon(
                        if (state.myWorkStatus) Icons.Rounded.Verified else Icons.Rounded.VisibilityOff,
                        null,
                        tint = if (state.myWorkStatus) LuxeColors.Green else Color.White.copy(0.2f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max), 
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                color = Color.White.copy(0.03f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    LaunchedEffect(isPressed) { onMicPro(isPressed, myPower) }

                    val infiniteTransition = rememberInfiniteTransition()
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1f, targetValue = 1.08f,
                        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse)
                    )
                    val glowAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f, targetValue = 0.7f,
                        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse)
                    )

                    Box(
                        Modifier
                            .size(44.dp)
                            .scale(if(isPressed) 0.9f else pulseScale)
                            .background(
                                if(isPressed) LuxeColors.Red.copy(0.2f) 
                                else LuxeColors.ElectricBlue.copy(0.1f * glowAlpha), 
                                CircleShape
                            )
                            .border(
                                1.5.dp, 
                                if(isPressed) LuxeColors.Red 
                                else LuxeColors.ElectricBlue.copy(glowAlpha), 
                                CircleShape
                            )
                            .pointerInput(state.hasAcceptedMicExplain, state.isJustBrowsing) {
                                detectTapGestures(
                                    onPress = { offset ->
                                        // =======================================================
                                        // 🔒 HARD-LOCK: PROFESSIONAL MIC GUARD
                                        // Protección contra transmisiones mudas sin permiso.
                                        // =======================================================
                                        if (state.isJustBrowsing || state.myProRole == "CIUDADANO") {
                                            showMicRestrictedDialog = true
                                            return@detectTapGestures
                                        }

                                        if (state.hasAcceptedMicExplain) {
                                            val press = androidx.compose.foundation.interaction.PressInteraction.Press(offset)
                                            interactionSource.emit(press)
                                            tryAwaitRelease()
                                            interactionSource.emit(androidx.compose.foundation.interaction.PressInteraction.Release(press))
                                        } else {
                                            onNotification(AppNotification(
                                                "PERMISO REQUERIDO",
                                                "Debes autorizar el micrófono en la pantalla de inicio.",
                                                NotificationType.Warning
                                            ))
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if(isPressed) Icons.Rounded.Mic else Icons.Rounded.MicNone, 
                            null, 
                            tint = if(isPressed) LuxeColors.Red else LuxeColors.ElectricBlue, 
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("DILO ASÍ (PTT):", color = Color.White.copy(0.4f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        val roleName = getRoleById(state.myProRole).name
                        Text(
                            when {
                                state.myProRole == "CIUDADANO" -> "⚠️ CONFIGURA TU PERFIL PARA HABLAR"
                                state.isProSeeking -> "Busco $roleName"
                                else -> "Soy $roleName disponible"
                            },
                            color = if (state.myProRole == "CIUDADANO") LuxeColors.Gold else Color.White,
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            if (state.myProRole == "CIUDADANO") "Pulsa el micro para elegir perfil." 
                            else "Pulsa el micro para hablar.", 
                            color = LuxeColors.ElectricBlue, 
                            fontSize = 8.sp, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            val infiniteTransition = rememberInfiniteTransition()
            val radarGlow by infiniteTransition.animateFloat(0.4f, 1f, infiniteRepeatable(tween(800), RepeatMode.Reverse))

            Surface(
                modifier = Modifier
                    .width(140.dp)
                    .fillMaxHeight()
                    .clickable { 
                        if (!state.hasSeenRadarMagic) onShowRadarMagic() else onReplayPro()
                        showOfferAlert = false 
                        triggerUiSound("click")
                    },
                color = if (showOfferAlert) LuxeColors.Gold.copy(0.2f) else LuxeColors.Gold.copy(0.12f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (showOfferAlert) LuxeColors.Gold.copy(radarGlow) else LuxeColors.Gold.copy(0.3f))
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (showOfferAlert) Icons.Rounded.RecordVoiceOver else Icons.Rounded.SettingsVoice, null, tint = LuxeColors.Gold, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(if (showOfferAlert) "ANUNCIO: $lastEmployerNick" else "RADAR PRO", color = LuxeColors.Gold, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.widthIn(max = 80.dp).basicMarquee())
                        Text(if (showOfferAlert) "PULSA PARA OÍR" else "OÍR SERVICIOS", color = if (showOfferAlert) Color.White else Color.White.copy(0.7f), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        if (showOfferAlert) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ANUNCIO DETECTADO: $lastEmployerNick", color = LuxeColors.Gold.copy(0.6f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(12.dp))
                Surface(
                    onClick = { onClose() },
                    color = LuxeColors.ElectricBlue.copy(0.1f),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, LuxeColors.ElectricBlue.copy(0.3f))
                ) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Rounded.Chat, null, tint = LuxeColors.ElectricBlue, modifier = Modifier.size(10.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("CONTACTAR", color = LuxeColors.ElectricBlue, fontSize = 8.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        LuxeButton(
            text = "COMPARTIR TERMINAL PRO",
            onClick = onShowSocialShare,
            enabled = true,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            containerColor = LuxeColors.ElectricBlue.copy(0.2f),
            contentColor = Color.White,
            icon = Icons.Rounded.Share
        )
        
        Text("Comparte el terminal con otros profesionales para ampliar la red.", color = Color.White.copy(0.3f), fontSize = 8.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))

        Spacer(Modifier.height(32.dp))

        Text("PROFESIONALES EN ${state.city}", color = Color.White.copy(0.3f), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        Spacer(Modifier.height(12.dp))

        val proUsers = users.filter { it.city == state.city && it.proRole != "CIUDADANO" }
        
        if (proUsers.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                Text("NO HAY OTROS PROFESIONALES ACTIVOS EN ESTA CIUDAD", color = Color.White.copy(0.1f), fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                proUsers.forEach { user ->
                    val role = getRoleById(user.proRole)
                    val infiniteSosTransition = rememberInfiniteTransition()
                    val sosAlpha by infiniteSosTransition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(800), RepeatMode.Reverse))

                    Surface(
                        color = if (user.isSOS) LuxeColors.Red.copy(alpha = 0.1f * sosAlpha) else Color.White.copy(0.05f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, if (user.isSOS) LuxeColors.Red.copy(alpha = 0.6f * sosAlpha) else Color.White.copy(0.1f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(36.dp).clip(CircleShape).background(if (user.isWorkAvailable) LuxeColors.Green.copy(0.2f) else Color.White.copy(0.1f)), contentAlignment = Alignment.Center) {
                                Icon(role.icon, null, tint = if (user.isWorkAvailable) LuxeColors.Green else Color.White.copy(0.4f), modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(user.nick, color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val label = if (user.isProSeeking) "BUSCO ${role.name}" else "${role.name} DISPONIBLE"
                                    Text(label, color = if (user.isProSeeking) LuxeColors.Gold else LuxeColors.ElectricBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            if (user.isSOS && user.gpsUrl != null) {
                                IconButton(onClick = { 
                                    onNotification(AppNotification("LOCALIZACIÓN GPS", "Abriendo posición de ${user.nick}...", NotificationType.Info))
                                    uriHandler.openUri(user.gpsUrl!!)
                                }, modifier = Modifier.size(32.dp).background(LuxeColors.Red.copy(0.1f), CircleShape)) {
                                    Icon(Icons.Rounded.LocationOn, null, tint = LuxeColors.Red, modifier = Modifier.size(16.dp))
                                }
                                Spacer(Modifier.width(8.dp))
                            }

                            IconButton(onClick = { onReportPro(user.id) }, modifier = Modifier.size(32.dp).background(Color.White.copy(0.03f), CircleShape)) {
                                Icon(Icons.Rounded.GppBad, null, tint = LuxeColors.Red.copy(0.6f), modifier = Modifier.size(14.dp))
                            }

                            if (user.isSOS) {
                                Icon(Icons.Rounded.Warning, null, tint = LuxeColors.Red, modifier = Modifier.size(20.dp).graphicsLayer(alpha = sosAlpha))
                            } else if (user.isWorkAvailable) {
                                Surface(color = LuxeColors.Green, shape = CircleShape) {
                                    Text("DISPONIBLE", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = Color.Black, fontSize = 7.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        LuxeButton(
            text = "SALIR A RADIO",
            onClick = onClose,
            enabled = true,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            containerColor = Color.White.copy(0.1f),
            contentColor = Color.White
        )

        Spacer(Modifier.height(12.dp))
        Text("Uso exclusivo para emergencias reales. El mal uso conlleva baneo permanente.", color = Color.White.copy(0.4f), fontSize = 8.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun ProfessionalTutorial(
    step: Int,
    onNext: () -> Unit,
    onClose: () -> Unit
) {
    val totalSteps = 4
    if (step > totalSteps) {
        onClose()
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(0.8f))
            .clickable { onNext() },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.padding(32.dp).fillMaxWidth(),
            color = Color(0xFF1A1A1A),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, LuxeColors.ElectricBlue.copy(0.3f))
        ) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("PASO $step de $totalSteps", color = LuxeColors.ElectricBlue, fontSize = 10.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(16.dp))
                
                when(step) {
                    1 -> {
                        Icon(Icons.Rounded.TouchApp, null, tint = LuxeColors.Gold, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("1. ELIGE TU PERFIL", fontWeight = FontWeight.Black, color = Color.White, fontSize = 18.sp)
                        Text("Selecciona tu oficio o el servicio que buscas. Esto es vital: solo recibirás avisos que coincidan con tu etiqueta.", textAlign = TextAlign.Center, color = Color.White.copy(0.7f), modifier = Modifier.padding(top = 8.dp))
                        Text("📌 Ejemplo: Si eres Fontanero, solo te avisaremos cuando alguien busque un fontanero.", fontSize = 11.sp, color = LuxeColors.ElectricBlue, modifier = Modifier.padding(top = 4.dp))
                    }
                    2 -> {
                        Icon(Icons.Rounded.RadioButtonChecked, null, tint = LuxeColors.Green, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("2. ACTIVA TU ESTADO", fontWeight = FontWeight.Black, color = Color.White, fontSize = 18.sp)
                        Text("Pulsa el botón grande de arriba para ponerte en 'DISPONIBLE'.", textAlign = TextAlign.Center, color = Color.White.copy(0.7f), modifier = Modifier.padding(top = 8.dp))
                        Text("📌 Nota: Si estás en 'MODO ESCUCHA', nadie podrá encontrarte.", fontSize = 11.sp, color = LuxeColors.ElectricBlue, modifier = Modifier.padding(top = 4.dp))
                    }
                    3 -> {
                        Icon(Icons.Rounded.Mic, null, tint = LuxeColors.ElectricBlue, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("3. HABLA POR LA RADIO", fontWeight = FontWeight.Black, color = Color.White, fontSize = 18.sp)
                        Text("Sal del terminal y usa el botón circular de la radio para hablar en directo.", textAlign = TextAlign.Center, color = Color.White.copy(0.7f), modifier = Modifier.padding(top = 8.dp))
                        Text("📌 Ejemplo: 'Hola, soy fontanero y estoy libre en la calle Mayor'.", fontSize = 11.sp, color = LuxeColors.ElectricBlue, modifier = Modifier.padding(top = 4.dp))
                    }
                    4 -> {
                        Icon(Icons.Rounded.Verified, null, tint = LuxeColors.Gold, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("4. GANA REPUTACIÓN", fontWeight = FontWeight.Black, color = Color.White, fontSize = 18.sp)
                        Text("Si eres un buen profesional, tu nivel de confianza en la red aumentará con cada colaboración.", textAlign = TextAlign.Center, color = Color.White.copy(0.7f), modifier = Modifier.padding(top = 8.dp))
                        Text("📌 Consejo: Una buena reputación te garantiza prioridad en los anuncios del Radar Pro.", fontSize = 11.sp, color = LuxeColors.ElectricBlue, modifier = Modifier.padding(top = 4.dp))
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                Text("PULSA PARA CONTINUAR", color = LuxeColors.ElectricBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SocialShareSheet(
    state: RadioState,
    onDismiss: () -> Unit,
    onShareAction: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF121212),
        modifier = Modifier.border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp)),
        title = {
            Text("¡HAZLO VIRAL!", color = LuxeColors.Gold, fontWeight = FontWeight.Black, fontSize = 18.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Selecciona dónde quieres compartir tu Terminal Profesional:", color = Color.White.copy(0.7f), fontSize = 12.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(24.dp))
                val networks = listOf("WhatsApp" to LuxeColors.Green, "Instagram" to Color(0xFFE4405F), "Facebook" to Color(0xFF1877F2), "TikTok" to Color.White, "InfoJobs" to Color(0xFF00539C))
                LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.height(200.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    gridItems(networks) { (name, color) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onShareAction(name) }) {
                            Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(color.copy(0.1f)).border(1.dp, color.copy(0.3f), CircleShape), contentAlignment = Alignment.Center) {
                                val icon = when(name) {
                                    "WhatsApp" -> Icons.AutoMirrored.Rounded.Chat
                                    "Instagram" -> Icons.Rounded.CameraAlt
                                    "Facebook" -> Icons.Rounded.Public
                                    "TikTok" -> Icons.Rounded.MusicNote
                                    "InfoJobs" -> Icons.Rounded.Work
                                    else -> Icons.Rounded.Share
                                }
                                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(name, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            LuxeButton(text = "CANCELAR", onClick = onDismiss, enabled = true, modifier = Modifier.fillMaxWidth().height(48.dp), containerColor = Color.White.copy(0.05f), contentColor = Color.White)
        }
    )
}
