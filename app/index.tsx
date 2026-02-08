import * as Location from "expo-location";
import { Accelerometer, Gyroscope } from "expo-sensors";
import React, { useEffect, useState } from "react";
import {
  ActivityIndicator,
  Alert,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from "react-native";
import MapView, { Marker } from "react-native-maps";

export default function Index() {
  // ---------- STATE ----------
  const [location, setLocation] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  const [fallPending, setFallPending] = useState(false); // 3-sec confirmation
  const [fallDetected, setFallDetected] = useState(false); // confirmed fall

  // ---------- GPS LOCATION ----------
  useEffect(() => {
    (async () => {
      let { status } = await Location.requestForegroundPermissionsAsync();
      if (status !== "granted") {
        setErrorMsg("Permission to access location was denied");
        setLoading(false);
        return;
      }

      let loc = await Location.getCurrentPositionAsync({});
      setLocation(loc.coords);
      setLoading(false);
    })();
  }, []);

  // ---------- SENSOR-BASED FALL DETECTION ----------
  useEffect(() => {
    let accelSub: any;
    let gyroSub: any;

    const ACCEL_THRESHOLD = 2.5;
    const GYRO_THRESHOLD = 3.0;

    accelSub = Accelerometer.addListener(({ x, y, z }) => {
      const magnitude = Math.sqrt(x * x + y * y + z * z);
      if (magnitude > ACCEL_THRESHOLD && !fallPending && !fallDetected) {
        setFallPending(true);
      }
    });

    gyroSub = Gyroscope.addListener(({ x, y, z }) => {
      const magnitude = Math.sqrt(x * x + y * y + z * z);
      if (magnitude > GYRO_THRESHOLD && !fallPending && !fallDetected) {
        setFallPending(true);
      }
    });

    Accelerometer.setUpdateInterval(500);
    Gyroscope.setUpdateInterval(500);

    return () => {
      accelSub && accelSub.remove();
      gyroSub && gyroSub.remove();
    };
  }, [fallPending, fallDetected]);

  // ---------- 3 SECOND CONFIRMATION TIMER ----------
  useEffect(() => {
    if (fallPending) {
      const timer = setTimeout(() => {
        setFallDetected(true); // confirmed emergency
        setFallPending(false);
      }, 3000);

      return () => clearTimeout(timer);
    }
  }, [fallPending]);

  // ---------- ALERT AFTER CONFIRMED FALL ----------
  useEffect(() => {
    if (fallDetected) {
      Alert.alert(
        "Fall Detected",
        "No response received. Emergency alert triggered."
      );
    }
  }, [fallDetected]);

  // ---------- LOADING ----------
  if (loading) {
    return (
      <View style={styles.centered}>
        <ActivityIndicator size="large" />
        <Text>Getting your location...</Text>
      </View>
    );
  }

  // ---------- ERROR ----------
  if (errorMsg) {
    return (
      <View style={styles.centered}>
        <Text style={{ color: "red" }}>{errorMsg}</Text>
      </View>
    );
  }

  // ---------- MAIN UI ----------
  return (
    <View style={styles.container}>
      <MapView
        style={styles.map}
        initialRegion={{
          latitude: location.latitude,
          longitude: location.longitude,
          latitudeDelta: 0.01,
          longitudeDelta: 0.01,
        }}
        showsUserLocation={true}
      >
        {/* Normal marker */}
        <Marker
          coordinate={{
            latitude: location.latitude,
            longitude: location.longitude,
          }}
          pinColor="blue"
          title="You are here"
        />

        {/* Emergency marker (only when fall detected) */}
        {fallDetected && (
          <Marker
            coordinate={{
              latitude: location.latitude,
              longitude: location.longitude,
            }}
            pinColor="red"
            title="Emergency Location"
          />
        )}
      </MapView>

      {/* ---------- FALL CONFIRMATION UI ---------- */}
      {fallPending && (
        <View style={styles.confirmBox}>
          <Text style={styles.warningText}>
            Fall detected. Cancel within 3 seconds?
          </Text>

          <TouchableOpacity
            onPress={() => setFallPending(false)}
            style={styles.cancelBtn}
          >
            <Text style={{ color: "white" }}>CANCEL</Text>
          </TouchableOpacity>
        </View>
      )}

      {/* ---------- DEBUG STATUS ---------- */}
      <View style={styles.debugBox}>
        <Text>Fall pending: {fallPending ? "YES" : "NO"}</Text>
        <Text>Fall confirmed: {fallDetected ? "YES" : "NO"}</Text>
      </View>
    </View>
  );
}

// ---------- STYLES ----------
const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  map: {
    flex: 1,
  },
  centered: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
  },
  confirmBox: {
    padding: 12,
    backgroundColor: "#fff3cd",
    alignItems: "center",
  },
  warningText: {
    color: "#856404",
    fontWeight: "bold",
    marginBottom: 8,
  },
  cancelBtn: {
    backgroundColor: "#007bff",
    paddingHorizontal: 20,
    paddingVertical: 6,
    borderRadius: 5,
  },
  debugBox: {
    padding: 10,
    backgroundColor: "white",
    alignItems: "center",
  },
});




