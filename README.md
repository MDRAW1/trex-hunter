# T-Rex Hunter (Fabric, Minecraft 26.3)

Um tiranossauro aterrorizante que te caça aleatoriamente.

## O que faz

- **Caçada aleatória**: de tempos em tempos (padrão 12–25 min) um T-Rex surge a 38–56 blocos na sua frente.
  Ele ruge, aparece o aviso na tela e começa a **contagem de 20 segundos** (na action bar) —
  esse é o tempo para se esconder ou fugir. Quando chega a zero ele corre atrás de você.
  Ele precisa te enxergar (agachado = só de perto; correndo = ele ouve). Se perder o rastro por ~45s, desiste e some.
- **Observador (estilo From the Fog)**: de vez em quando (padrão 6–14 min) um T-Rex fica parado ao longe,
  só te encarando. É invulnerável e silencioso. Se você olhar fixo por ~3s ou chegar perto, ele some.
  Raramente (15%) o observador se revela e vira uma caçada de verdade.
- **Spawn egg** na aba "Ovos de Spawn" do criativo (também `/summon trexhunter:trex`).
  Ovo/summon cria um T-Rex "normal" (contagem de 20s + caçada).

## Config

`config/trexhunter.properties` (criado na primeira execução):

| chave | padrão | efeito |
|---|---|---|
| enableHunts / enableWatchers | true | liga/desliga cada comportamento |
| onlyOverworld | true | só no Overworld |
| affectCreative | false | caçadas e observadores no modo criativo |
| countdownSeconds | 20 | tempo para se esconder |
| huntMin/MaxMinutes, watchMin/MaxMinutes | 12/25, 6/14 | intervalos aleatórios |
| debugFastTimers | false | **teste**: caçada a cada 30–60s, observador a cada 15–30s |

Não acontece em dificuldade Pacífica.

## Compilar

Requer **JDK 25** e Gradle (use o wrapper de qualquer template do Fabric, ou `gradle wrapper`).

```
gradle build
```

O jar sai em `build/libs/trex-hunter-1.0.0.jar` → coloque em `mods/` junto com o Fabric API 0.160.5+26.3.
Para testar em desenvolvimento: `gradle runClient`.
