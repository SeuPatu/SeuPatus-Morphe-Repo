package app.template.extension.tomato;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.lang.reflect.Field;

/**
 * Chamado pelo patch logo a seguir a CastButtonFactory.setUpMediaRouteButton().
 * Intercepta o toque no botão de cast e abre o stream no VLC.
 * Usa reflexão para não depender das classes do Cast/Media3 em tempo de compilação.
 */
@SuppressWarnings("unused")
public final class VlcCastPatch {
    private static final String VLC_PACKAGE = "org.videolan.vlc";

    public static void hook(View castButton) {
        castButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    openInVlc(v.getContext());
                }
                // Consumir o toque impede o MediaRouteButton de abrir o diálogo do Google Cast.
                return true;
            }
        });
    }

    private static void openInVlc(Context context) {
        Activity activity = unwrap(context);
        if (activity == null) return;

        try {
            String url = (String) readField(activity, "defaultURL");
            if (url == null || url.isEmpty()) {
                Toast.makeText(activity, "Stream ainda não carregou", Toast.LENGTH_SHORT).show();
                return;
            }

            long positionMs = 0;
            Object player = readStaticField(activity.getClass(), "player");
            if (player != null) {
                positionMs = (Long) player.getClass().getMethod("getCurrentPosition").invoke(player);
                player.getClass().getMethod("pause").invoke(player);
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndTypeAndNormalize(Uri.parse(url), "video/*");
            intent.setPackage(VLC_PACKAGE);
            intent.putExtra("position", positionMs);
            intent.putExtra("from_start", false);
            // TODO: intent.putExtra("headers", new String[]{"Referer", "...", "User-Agent", "..."});
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, "VLC não está instalado", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(activity, "Falha ao abrir no VLC: " + e, Toast.LENGTH_LONG).show();
        }
    }

    private static Activity unwrap(Context c) {
        while (c instanceof ContextWrapper) {
            if (c instanceof Activity) return (Activity) c;
            c = ((ContextWrapper) c).getBaseContext();
        }
        return null;
    }

    private static Object readField(Object o, String name) throws Exception {
        Field f = o.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(o);
    }

    private static Object readStaticField(Class<?> c, String name) throws Exception {
        Field f = c.getDeclaredField(name);
        f.setAccessible(true);
        return f.get(null);
    }
}
