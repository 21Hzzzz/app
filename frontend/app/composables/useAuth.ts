export type UserRole = "user" | "admin";

export interface UserInfo {
  id: number;
  username: string;
  role: UserRole;
}

type StoredAuth = {
  isLoggedIn: boolean;
  user: UserInfo | null;
};

const STORAGE_KEY = "auth";

function isValidUserInfo(data: unknown): data is UserInfo {
  if (!data || typeof data !== "object") {
    return false;
  }

  const user = data as Record<string, unknown>;

  return (
    typeof user.id === "number" &&
    typeof user.username === "string" &&
    (user.role === "user" || user.role === "admin")
  );
}

export const useAuth = () => {
  const isLoggedIn = useState<boolean>("auth:isLoggedIn", () => false);
  const user = useState<UserInfo | null>("auth:user", () => null);
  const inited = useState<boolean>("auth:inited", () => false);

  const username = computed(() => user.value?.username ?? "");
  const isAdmin = computed(() => user.value?.role === "admin");

  function resetAuth() {
    isLoggedIn.value = false;
    user.value = null;
  }

  function persistAuth() {
    if (!import.meta.client) {
      return;
    }

    if (!isLoggedIn.value || !user.value) {
      localStorage.removeItem(STORAGE_KEY);
      return;
    }

    const payload: StoredAuth = {
      isLoggedIn: true,
      user: user.value,
    };

    localStorage.setItem(STORAGE_KEY, JSON.stringify(payload));
  }

  function initAuth() {
    if (!import.meta.client || inited.value) {
      return;
    }

    const raw = localStorage.getItem(STORAGE_KEY);

    if (!raw) {
      inited.value = true;
      return;
    }

    try {
      const data = JSON.parse(raw) as Partial<StoredAuth>;

      if (data.isLoggedIn === true && isValidUserInfo(data.user)) {
        isLoggedIn.value = true;
        user.value = data.user;
      } else {
        resetAuth();
        localStorage.removeItem(STORAGE_KEY);
      }
    } catch {
      resetAuth();
      localStorage.removeItem(STORAGE_KEY);
    }

    inited.value = true;
  }

  function login(loginUser: UserInfo) {
    isLoggedIn.value = true;
    user.value = loginUser;
    persistAuth();
  }

  function logout() {
    resetAuth();

    if (import.meta.client) {
      localStorage.removeItem(STORAGE_KEY);
    }
  }

  return {
    isLoggedIn,
    user,
    username,
    isAdmin,
    inited,
    initAuth,
    login,
    logout,
  };
};
