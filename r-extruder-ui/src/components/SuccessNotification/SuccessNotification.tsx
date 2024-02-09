import * as React from "react";
import { useEffect, useState } from "react";
import {
  Notification,
  NotificationGroup,
} from "@progress/kendo-react-notification";
import { Fade } from "@progress/kendo-react-animation";
import { SuccessNotificationProps } from "../../utils/types";
import "./SuccessNotification.scss";

const SuccessNotification: React.FC<SuccessNotificationProps> = ({
  successConversion,
  onClose,
}) => {
  const [notificationState, setNotificationState] = useState(successConversion);

  useEffect(() => {
    setNotificationState(successConversion);
  }, [successConversion]);
  const handleNotificationClose = () => {
    if (onClose) {
      onClose(false);
    }
    setNotificationState(false);
  };

  return (
    <NotificationGroup
      style={{
        right: 200,
        top: 10,
        alignItems: "flex-start",
        flexWrap: "wrap-reverse",
      }}
    >
      <Fade>
        {notificationState && (
          <Notification
            type={{ style: "success", icon: true }}
            closable={true}
            onClose={handleNotificationClose}
            style={{ display: "flex", flexDirection: "row-reverse" }}
          >
            <span>Your data has been saved.</span>
          </Notification>
        )}
      </Fade>
    </NotificationGroup>
  );
};

export default SuccessNotification;
