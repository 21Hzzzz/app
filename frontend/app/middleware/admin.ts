export default defineNuxtRouteMiddleware(() => {
  const { isLoggedIn, isAdmin, initAuth, inited } = useAuth();

  if (import.meta.server) {
    return;
  }

  if (!inited.value) {
    initAuth();
  }

  if (!isLoggedIn.value) {
    return navigateTo("/login");
  }

  if (!isAdmin.value) {
    return navigateTo("/admin/forbidden");
  }
});
