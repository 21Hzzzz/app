export type ChatUnreadSession = {
  id: number;
  orderId: number;
  buyerId: number;
  buyerUsername?: string | null;
  sellerId: number;
  sellerUsername?: string | null;
  latestMessageId?: number | null;
  latestMessageSenderId?: number | null;
  latestMessage?: string | null;
  latestMessageTime?: string | null;
  unreadCount?: number | null;
  createTime: string;
};

type ChatUnreadApiResponse<T> = {
  code: number;
  message: string;
  data: T;
};

type MarkChatSessionReadRequest = {
  userId: number;
};

const CHAT_UNREAD_POLLING_INTERVAL = 5000;

let chatUnreadPollingTimer: ReturnType<typeof setInterval> | null = null;
let chatUnreadFocusHandler: (() => void) | null = null;
let chatUnreadVisibilityHandler: (() => void) | null = null;

function normalizeUnreadCount(value?: number | null) {
  if (typeof value !== "number" || !Number.isFinite(value) || value < 0) {
    return 0;
  }

  return value;
}

function normalizeSession(session: ChatUnreadSession): ChatUnreadSession {
  return {
    ...session,
    unreadCount: normalizeUnreadCount(session.unreadCount),
  };
}

function sumUnreadMessages(sessions: ChatUnreadSession[]) {
  return sessions.reduce((total, session) => {
    return total + normalizeUnreadCount(session.unreadCount);
  }, 0);
}

function removeSessionId(items: number[], sessionId: number) {
  return items.filter((item) => item !== sessionId);
}

export const useChatUnread = () => {
  const api = useApi();
  const { user } = useAuth();

  const allSessions = useState<ChatUnreadSession[]>("chat-unread:all-sessions", () => []);
  const refreshing = useState<boolean>("chat-unread:refreshing", () => false);
  const loadError = useState<string>("chat-unread:load-error", () => "");
  const markingSessionIds = useState<number[]>(
    "chat-unread:marking-session-ids",
    () => []
  );
  const activeUserId = useState<number>("chat-unread:active-user-id", () => 0);
  const lastMarkedMessageIds = useState<Record<string, number>>(
    "chat-unread:last-marked-message-ids",
    () => ({})
  );

  const currentUserId = computed(() => user.value?.id ?? 0);

  const buyerSessions = computed(() => {
    if (!currentUserId.value) {
      return [];
    }

    return allSessions.value.filter(
      (session) => session.buyerId === currentUserId.value
    );
  });

  const sellerSessions = computed(() => {
    if (!currentUserId.value) {
      return [];
    }

    return allSessions.value.filter(
      (session) => session.sellerId === currentUserId.value
    );
  });

  const unreadSessions = computed(() =>
    allSessions.value.filter((session) => normalizeUnreadCount(session.unreadCount) > 0)
  );

  const buyerUnreadSessions = computed(() =>
    buyerSessions.value.filter((session) => normalizeUnreadCount(session.unreadCount) > 0)
  );

  const sellerUnreadSessions = computed(() =>
    sellerSessions.value.filter((session) => normalizeUnreadCount(session.unreadCount) > 0)
  );

  const hasUnread = computed(() => sumUnreadMessages(allSessions.value) > 0);
  const unreadSessionCount = computed(() => unreadSessions.value.length);
  const totalUnreadMessageCount = computed(() => sumUnreadMessages(allSessions.value));
  const buyerUnreadSessionCount = computed(() => buyerUnreadSessions.value.length);
  const buyerTotalUnreadMessageCount = computed(() =>
    sumUnreadMessages(buyerSessions.value)
  );
  const sellerUnreadSessionCount = computed(() => sellerUnreadSessions.value.length);
  const sellerTotalUnreadMessageCount = computed(() =>
    sumUnreadMessages(sellerSessions.value)
  );

  function clearState() {
    allSessions.value = [];
    loadError.value = "";
    markingSessionIds.value = [];
    activeUserId.value = 0;
    lastMarkedMessageIds.value = {};
  }

  function applySessions(sessions: ChatUnreadSession[]) {
    allSessions.value = sessions.map(normalizeSession);
  }

  function updateSession(sessionId: number, updater: (session: ChatUnreadSession) => ChatUnreadSession) {
    allSessions.value = allSessions.value.map((session) => {
      if (session.id !== sessionId) {
        return session;
      }

      return normalizeSession(updater(session));
    });
  }

  async function refresh() {
    if (!import.meta.client) {
      return;
    }

    const userId = user.value?.id ?? 0;

    if (!userId) {
      clearState();
      return;
    }

    if (activeUserId.value && activeUserId.value !== userId) {
      clearState();
    }

    activeUserId.value = userId;

    if (refreshing.value) {
      return;
    }

    refreshing.value = true;
    loadError.value = "";

    try {
      const response = await api<ChatUnreadApiResponse<ChatUnreadSession[]>>(
        "/api/chat/sessions/all",
        {
          method: "GET",
          query: { userId },
        }
      );

      if (response.code !== 200) {
        throw new Error(response.message || "获取会话未读状态失败");
      }

      applySessions(response.data || []);
    } catch (error) {
      loadError.value =
        error instanceof Error ? error.message : "获取会话未读状态失败";
    } finally {
      refreshing.value = false;
    }
  }

  async function markSessionRead(
    sessionId: number,
    latestKnownMessageId?: number | null
  ) {
    if (!import.meta.client) {
      return;
    }

    const userId = user.value?.id ?? 0;

    if (!userId) {
      return;
    }

    const session = allSessions.value.find((item) => item.id === sessionId);
    const effectiveLatestMessageId =
      typeof latestKnownMessageId === "number" && latestKnownMessageId > 0
        ? latestKnownMessageId
        : session?.latestMessageId ?? null;

    if (!effectiveLatestMessageId) {
      return;
    }

    const alreadyMarkedMessageId =
      lastMarkedMessageIds.value[String(sessionId)] ?? 0;
    const currentUnreadCount = normalizeUnreadCount(session?.unreadCount);

    if (
      alreadyMarkedMessageId >= effectiveLatestMessageId &&
      currentUnreadCount === 0
    ) {
      return;
    }

    if (markingSessionIds.value.includes(sessionId)) {
      return;
    }

    markingSessionIds.value = [...markingSessionIds.value, sessionId];

    try {
      const payload: MarkChatSessionReadRequest = { userId };
      const response = await api<ChatUnreadApiResponse<null>>(
        `/api/chat/sessions/${sessionId}/read`,
        {
          method: "POST",
          body: payload,
        }
      );

      if (response.code !== 200) {
        throw new Error(response.message || "标记会话已读失败");
      }

      lastMarkedMessageIds.value = {
        ...lastMarkedMessageIds.value,
        [String(sessionId)]: effectiveLatestMessageId,
      };

      updateSession(sessionId, (currentSession) => ({
        ...currentSession,
        unreadCount: 0,
      }));

      await refresh();
    } catch {
    } finally {
      markingSessionIds.value = removeSessionId(
        markingSessionIds.value,
        sessionId
      );
    }
  }

  function installVisibilityListeners() {
    if (!import.meta.client) {
      return;
    }

    if (!chatUnreadFocusHandler) {
      chatUnreadFocusHandler = () => {
        void refresh();
      };
      window.addEventListener("focus", chatUnreadFocusHandler);
    }

    if (!chatUnreadVisibilityHandler) {
      chatUnreadVisibilityHandler = () => {
        if (document.visibilityState === "visible") {
          void refresh();
        }
      };
      document.addEventListener("visibilitychange", chatUnreadVisibilityHandler);
    }
  }

  function removeVisibilityListeners() {
    if (!import.meta.client) {
      return;
    }

    if (chatUnreadFocusHandler) {
      window.removeEventListener("focus", chatUnreadFocusHandler);
      chatUnreadFocusHandler = null;
    }

    if (chatUnreadVisibilityHandler) {
      document.removeEventListener(
        "visibilitychange",
        chatUnreadVisibilityHandler
      );
      chatUnreadVisibilityHandler = null;
    }
  }

  function startPolling() {
    if (!import.meta.client) {
      return;
    }

    installVisibilityListeners();

    if (!chatUnreadPollingTimer) {
      chatUnreadPollingTimer = setInterval(() => {
        void refresh();
      }, CHAT_UNREAD_POLLING_INTERVAL);
    }

    void refresh();
  }

  function stopPolling() {
    if (chatUnreadPollingTimer) {
      clearInterval(chatUnreadPollingTimer);
      chatUnreadPollingTimer = null;
    }

    removeVisibilityListeners();
  }

  return {
    allSessions,
    buyerSessions,
    sellerSessions,
    unreadSessions,
    buyerUnreadSessions,
    sellerUnreadSessions,
    hasUnread,
    refreshing,
    loadError,
    unreadSessionCount,
    totalUnreadMessageCount,
    buyerUnreadSessionCount,
    buyerTotalUnreadMessageCount,
    sellerUnreadSessionCount,
    sellerTotalUnreadMessageCount,
    refresh,
    startPolling,
    stopPolling,
    markSessionRead,
  };
};
